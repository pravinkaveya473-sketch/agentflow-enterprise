package com.cjl.java_ai_agent.knowledge;

import com.cjl.java_ai_agent.common.exception.BusinessException;
import com.cjl.java_ai_agent.config.AppProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class KnowledgeService {
    private final KnowledgeDocumentPersistenceService documents;
    private final VectorStore vectorStore;
    private final ChatClient ragChatClient;
    private final AppProperties properties;
    private final KnowledgeFilePolicy filePolicy;
    private final TokenTextSplitter splitter = TokenTextSplitter.builder()
            .withChunkSize(700)
            .withMinChunkSizeChars(150)
            .withMinChunkLengthToEmbed(50)
            .withMaxNumChunks(2000)
            .withKeepSeparator(true)
            .build();

    public KnowledgeService(KnowledgeDocumentPersistenceService documents,
                            VectorStore vectorStore,
                            @Qualifier("ragChatClient") ChatClient ragChatClient,
                            AppProperties properties,
                            KnowledgeFilePolicy filePolicy) {
        this.documents = documents;
        this.vectorStore = vectorStore;
        this.ragChatClient = ragChatClient;
        this.properties = properties;
        this.filePolicy = filePolicy;
    }

    /**
     * 数据库事务只覆盖元数据状态修改；Tika 解析、Embedding 和 Redis 向量写入均在事务外执行。
     */
    public KnowledgeDocumentView ingest(String tenantId, String knowledgeBase,
                                        MultipartFile file, String username) {
        validateName(knowledgeBase, "knowledgeBase");
        validateName(tenantId, "tenantId");
        filePolicy.validateUpload(file);

        String documentId = null;
        try {
            byte[] bytes = file.getBytes();
            String sha256 = sha256(bytes);
            Optional<KnowledgeDocumentEntity> duplicate =
                    documents.duplicate(tenantId, knowledgeBase, sha256);
            if (duplicate.isPresent()) {
                return KnowledgeDocumentView.from(duplicate.get());
            }

            documentId = UUID.randomUUID().toString();
            Path directory = Paths.get(properties.rag().uploadDir(), tenantId, knowledgeBase).normalize();
            Files.createDirectories(directory);
            String filename = sanitize(file.getOriginalFilename());
            Path path = directory.resolve(documentId + "-" + filename).normalize();
            if (!path.startsWith(directory)) {
                throw new BusinessException("VALIDATION_ERROR", "非法文件路径");
            }
            Files.write(path, bytes);

            documents.create(new KnowledgeDocumentEntity(
                    documentId, knowledgeBase, tenantId, filename, file.getContentType(),
                    bytes.length, path.toString(), sha256, username));

            List<Document> extracted = new TikaDocumentReader(new FileSystemResource(path)).get();
            filePolicy.validateExtractedText(extracted);
            final String currentDocumentId = documentId;
            List<Document> enriched = extracted.stream()
                    .filter(document -> document.getText() != null && !document.getText().isBlank())
                    .map(document -> Document.builder()
                            .text(document.getText())
                            .metadata("tenantId", tenantId)
                            .metadata("knowledgeBase", knowledgeBase)
                            .metadata("documentId", currentDocumentId)
                            .metadata("filename", filename)
                            .build())
                    .toList();

            List<Document> chunks = splitter.apply(enriched);
            if (chunks.isEmpty()) {
                throw new BusinessException("KNOWLEDGE_INGEST_FAILED", "文档没有可入库文本");
            }
            vectorStore.add(chunks);
            return documents.markReady(documentId, chunks.size());
        }
        catch (IOException exception) {
            markFailedQuietly(documentId);
            throw new BusinessException("KNOWLEDGE_INGEST_FAILED", "文档读取失败", exception);
        }
        catch (RuntimeException exception) {
            markFailedQuietly(documentId);
            throw exception;
        }
    }

    public List<KnowledgeDocumentView> list(String tenantId, String knowledgeBase) {
        validateName(knowledgeBase, "knowledgeBase");
        validateName(tenantId, "tenantId");
        return documents.list(tenantId, knowledgeBase);
    }

    public RagAnswer ask(String tenantId, String knowledgeBase, String question) {
        List<Document> matched = retrieve(tenantId, knowledgeBase, question);
        if (matched.isEmpty()) {
            return new RagAnswer("知识库中没有足够依据回答这个问题。", List.of());
        }

        StringBuilder context = new StringBuilder();
        for (int index = 0; index < matched.size(); index++) {
            String text = Objects.requireNonNullElse(matched.get(index).getText(), "");
            if (context.length() + text.length() > properties.rag().maxContextCharacters()) {
                break;
            }
            context.append("\n[资料").append(index + 1).append("] 来源=")
                    .append(matched.get(index).getMetadata().get("filename"))
                    .append("\n").append(text).append("\n");
        }

        String answer = ragChatClient.prompt().user("""
                用户问题：%s

                检索上下文：
                %s

                请仅依据上下文作答；依据不足时明确说明。最后以“来源：”列出文件名。
                """.formatted(question, context)).call().content();

        List<RagAnswer.Citation> citations = matched.stream()
                .map(document -> new RagAnswer.Citation(
                        String.valueOf(document.getMetadata().get("documentId")),
                        String.valueOf(document.getMetadata().get("filename")),
                        document.getScore(),
                        excerpt(document.getText())))
                .toList();
        return new RagAnswer(answer, citations);
    }

    public List<Document> retrieve(String tenantId, String knowledgeBase, String question) {
        validateName(knowledgeBase, "knowledgeBase");
        validateName(tenantId, "tenantId");
        if (question == null || question.isBlank()) {
            throw new BusinessException("VALIDATION_ERROR", "检索问题不能为空");
        }
        /*
         * Redis VectorStore 的原生 metadata filter 需要在索引创建时显式声明字段类型。
         * 为保持自动配置稳定，第一版先过量召回，再在 Java 中执行强制租户过滤，
         * 确保其他用户文档绝不会进入 Prompt。生产超大知识库可改为手动 RedisVectorStore Bean，
         * 注册 tenantId/knowledgeBase TAG 字段后把过滤下推到 Redis。
         */
        int candidateTopK = Math.min(Math.max(properties.rag().topK() * 20, 50), 200);
        List<Document> result = vectorStore.similaritySearch(SearchRequest.builder()
                .query(question)
                .topK(candidateTopK)
                .similarityThreshold(properties.rag().similarityThreshold())
                .build());
        if (result == null) return List.of();
        return result.stream()
                .filter(document -> tenantId.equals(String.valueOf(document.getMetadata().get("tenantId"))))
                .filter(document -> knowledgeBase.equals(String.valueOf(document.getMetadata().get("knowledgeBase"))))
                .limit(properties.rag().topK())
                .toList();
    }

    private void markFailedQuietly(String id) {
        if (id == null) return;
        try { documents.markFailed(id); }
        catch (RuntimeException ignored) { /* 保留原始异常 */ }
    }

    private void validateName(String value, String field) {
        if (value == null || !value.matches("[A-Za-z0-9_-]{1,64}")) {
            throw new BusinessException("VALIDATION_ERROR",
                    field + "只能包含字母、数字、下划线和连字符");
        }
    }

    private String sanitize(String name) {
        String value = name == null ? "document" :
                name.replaceAll("[^A-Za-z0-9._\\-\\u4e00-\\u9fa5]", "_");
        return value.length() > 180 ? value.substring(value.length() - 180) : value;
    }

    private String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        }
        catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private String excerpt(String text) {
        if (text == null) return "";
        String compact = text.replaceAll("\\s+", " ").trim();
        return compact.length() > 180 ? compact.substring(0, 180) + "…" : compact;
    }
}
