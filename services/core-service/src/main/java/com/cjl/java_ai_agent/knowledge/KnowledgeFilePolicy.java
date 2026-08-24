package com.cjl.java_ai_agent.knowledge;

import com.cjl.java_ai_agent.common.exception.BusinessException;
import com.cjl.java_ai_agent.config.AppProperties;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** 文件入口策略：扩展名白名单与解析文本规模限制。 */
@Component
public class KnowledgeFilePolicy {

    private final Set<String> allowedExtensions;
    private final long maxExtractedCharacters;

    public KnowledgeFilePolicy(AppProperties properties) {
        List<String> configured = properties.rag().allowedExtensions();
        this.allowedExtensions = new HashSet<>((configured == null || configured.isEmpty())
                ? List.of("pdf", "doc", "docx", "txt", "md")
                : configured.stream().map(value -> value.toLowerCase(Locale.ROOT)).toList());
        this.maxExtractedCharacters = properties.rag().maxExtractedCharacters();
    }

    public void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("VALIDATION_ERROR", "上传文件为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank() || !filename.contains(".")) {
            throw new BusinessException("UNSUPPORTED_FILE_TYPE", "文件必须包含受支持的扩展名");
        }
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!allowedExtensions.contains(extension)) {
            throw new BusinessException("UNSUPPORTED_FILE_TYPE",
                    "不支持的文件类型：" + extension + "，允许类型=" + allowedExtensions);
        }
    }

    public void validateExtractedText(List<Document> documents) {
        long characters = documents.stream()
                .map(Document::getText)
                .filter(java.util.Objects::nonNull)
                .mapToLong(String::length)
                .sum();
        if (characters > maxExtractedCharacters) {
            throw new BusinessException("DOCUMENT_TOO_LARGE",
                    "文档解析文本超过限制，characters=" + characters);
        }
    }
}
