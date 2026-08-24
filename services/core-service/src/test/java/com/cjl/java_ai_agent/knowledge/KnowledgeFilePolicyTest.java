package com.cjl.java_ai_agent.knowledge;

import com.cjl.java_ai_agent.common.exception.BusinessException;
import com.cjl.java_ai_agent.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KnowledgeFilePolicyTest {

    private final KnowledgeFilePolicy policy = new KnowledgeFilePolicy(new AppProperties(
            null,
            null,
            null,
            new AppProperties.Rag(5, 0.65, 12_000, 10_000,
                    List.of("pdf", "doc", "docx", "txt", "md"), "./data/uploads"),
            null,
            null,
            null,
            null));

    @Test
    void shouldAcceptSupportedDocument() {
        MockMultipartFile file = new MockMultipartFile("file", "policy.md", "text/markdown",
                "# company policy".getBytes());

        assertDoesNotThrow(() -> policy.validateUpload(file));
    }

    @Test
    void shouldRejectExecutableFile() {
        MockMultipartFile file = new MockMultipartFile("file", "payload.exe",
                "application/octet-stream", new byte[] {1, 2, 3});

        BusinessException exception = assertThrows(BusinessException.class,
                () -> policy.validateUpload(file));
        assertEquals("UNSUPPORTED_FILE_TYPE", exception.getCode());
    }
}
