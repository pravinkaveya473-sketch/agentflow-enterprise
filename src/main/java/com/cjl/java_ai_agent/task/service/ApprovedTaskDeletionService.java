package com.cjl.java_ai_agent.task.service;

import com.cjl.java_ai_agent.approval.ApprovalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 审批消费与任务删除必须处在同一数据库事务中，删除失败时审批消费也会回滚。 */
@Service
public class ApprovedTaskDeletionService {
    private final ApprovalService approvals;
    private final TaskService tasks;

    public ApprovedTaskDeletionService(ApprovalService approvals, TaskService tasks) {
        this.approvals = approvals;
        this.tasks = tasks;
    }

    @Transactional
    public void delete(String approvalId, String taskId, String username) {
        approvals.consumeApproved(approvalId, "DELETE_TASK", taskId, username);
        tasks.delete(username, taskId);
    }
}
