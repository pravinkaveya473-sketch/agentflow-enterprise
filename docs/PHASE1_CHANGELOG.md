# AgentFlow Enterprise 第一轮改造记录

## 项目定位变化

原定位偏向“功能集合式 Java AI Agent 示例”。本轮将其收敛为：

> 面向企业协作场景的智能任务编排与安全执行平台。

## 新增功能

1. 项目聚合
   - ProjectEntity、ProjectService、ProjectController、ProjectTools
   - 创建幂等、乐观锁、归档和恢复
2. 任务领域增强
   - projectId、description、assigneeUsername、blockedReason
   - BLOCKED 状态、block/resume/update
   - 项目和状态筛选
   - 负责人必须是真实启用用户；负责人可查看并推进获分配任务
3. 任务活动审计
   - task_activity 表
   - 记录完整状态变化和操作者
4. Agent 工具扩充
   - createProject、listProjects、archiveProject
   - updateTask、blockTask、resumeTask
5. 审批加固
   - 乐观锁、consumedAt、过期扫描
6. 安全加固
   - 移除 HTTP Basic，只保留 JWT Bearer
   - 用户名格式校验
   - Docker Compose 强制关键密钥由环境变量提供
7. RAG 文件入口安全
   - 扩展名白名单
   - 解析文本字符上限
8. 持久化优化
   - AgentStep 从“删除全部再重插”改为“仅追加新增步骤”
9. 测试扩展
   - 项目状态机
   - 任务阻塞/恢复与终态保护
   - 审批消费和过期
   - 文件类型白名单

## 暂未完成

- Maven 全量构建和 Spring ApplicationContext 启动
- MySQL/Redis/Qwen 端到端测试
- Agent 审批后自动恢复原 Run
- 前端管理后台
- Testcontainers 集成测试
- 真实性能与准确率数据
