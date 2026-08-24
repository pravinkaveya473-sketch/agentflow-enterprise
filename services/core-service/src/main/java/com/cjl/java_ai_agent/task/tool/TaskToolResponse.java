package com.cjl.java_ai_agent.task.tool;

import com.cjl.java_ai_agent.task.dto.TaskView;
import java.util.List;

public record TaskToolResponse(boolean success, String code, String message, TaskView task,
                               List<TaskView> tasks, String approvalId) {
    public static TaskToolResponse task(String message, TaskView task) { return new TaskToolResponse(true,"SUCCESS",message,task,List.of(),null); }
    public static TaskToolResponse list(List<TaskView> tasks) { return new TaskToolResponse(true,"SUCCESS","查询成功",null,List.copyOf(tasks),null); }
    public static TaskToolResponse failure(String code,String message) { return new TaskToolResponse(false,code,message,null,List.of(),null); }
    public static TaskToolResponse approval(String id,String message) { return new TaskToolResponse(false,"APPROVAL_REQUIRED",message,null,List.of(),id); }
}
