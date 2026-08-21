# AgentFlow Enterprise 简历基线（完成运行验证后使用）

## 项目名称

AgentFlow Enterprise——企业级智能任务编排与安全执行平台

## 技术栈

Java 21、Spring Boot 4.1、Spring AI 2.0、Spring Security、MySQL、Flyway、Redis Stack、Redis VectorStore、Redis Chat Memory、Micrometer、Prometheus、Grafana、Docker Compose

## 当前可由源码支持的描述

- 设计项目与任务领域模型，将任务状态约束收敛到聚合根，支持创建、开始、阻塞、恢复、完成、取消等状态迁移，并通过乐观锁与活动表保留业务审计轨迹。
- 基于 Spring AI 手动 Tool Calling 构建显式 Agent 循环，统一接入项目、任务和知识库工具，并增加模型/工具调用上限、总超时、重复调用检测及 AgentRun/AgentStep 持久化。
- 使用 MySQL 保存业务事实、审批和运行审计，使用 Redis 实现幂等、限流、运行缓存、多轮记忆与向量检索；AgentStep 改为增量追加，避免每轮全量删除重写。
- 针对永久删除任务建立人工审批链路，通过乐观锁、审批过期、一次性消费以及“审批消费 + 业务副作用”同事务控制重复执行和状态不一致。
- 对文档入库增加扩展名白名单、文本规模限制、SHA-256 去重和用户级知识库隔离，并返回检索引用来源。

## 暂时不得写入简历的内容

- 不得写具体吞吐量、P95 延迟、准确率或成本下降比例，当前尚无真实测试结果。
- 不得写“生产上线”“高可用集群”或“支持大规模用户”，当前尚未完成环境部署验证。
- 不得声称已完成 Agent 计划编排、审批后自动恢复和前端管理后台，这些属于后续阶段。
