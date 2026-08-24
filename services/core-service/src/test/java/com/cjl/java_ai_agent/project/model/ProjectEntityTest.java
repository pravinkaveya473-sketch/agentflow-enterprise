package com.cjl.java_ai_agent.project.model;

import com.cjl.java_ai_agent.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProjectEntityTest {

    @Test
    void shouldArchiveAndRestoreProject() {
        ProjectEntity project = new ProjectEntity("project-1", "cjl", "AgentFlow", "企业任务 Agent", null);

        project.archive();
        assertEquals(ProjectStatus.ARCHIVED, project.getStatus());

        project.restore();
        assertEquals(ProjectStatus.ACTIVE, project.getStatus());
    }

    @Test
    void shouldRejectEditingArchivedProject() {
        ProjectEntity project = new ProjectEntity("project-2", "cjl", "AgentFlow", null, null);
        project.archive();

        BusinessException exception = assertThrows(BusinessException.class,
                () -> project.rename("新名称", "新描述"));
        assertEquals("INVALID_PROJECT_STATE", exception.getCode());
    }
}
