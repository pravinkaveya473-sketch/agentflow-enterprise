# AgentFlow Enterprise：5 分钟面试演示

这套演示围绕一条真实业务主线展开，用尽量少的页面说明项目的业务完整性、微服务可靠性和 Agent 安全边界。

## 演示前准备

```powershell
Copy-Item .env.example .env
# 填写 .env 必填值；仅演示基础设施与任务事件时可保持 VECTOR_STORE_INITIALIZE_SCHEMA=false
.\mvnw.cmd clean verify
docker compose up -d --build
docker compose ps
```

预期结果：19 项测试通过；Gateway、Core、Event、MySQL、Redis、Kafka 全部健康。

## 0:00–0:40：一句话与架构

> AgentFlow 不是聊天 Demo，而是一个让 AI Agent 在任务状态机、RBAC、人工审批和审计边界内执行工作的企业级 Java 工程。3.0 通过 Gateway、Core、Kafka 和独立 Event Service 完成了小规模微服务拆分。

打开 README 架构图，按以下顺序讲解：

1. Gateway 是唯一公开业务入口。
2. Core 保存任务业务事实，并在同一事务中写 Outbox。
3. Publisher 将事件投递 Kafka，Event Service 幂等消费形成独立审计读模型。
4. Prometheus 分别采集三个 Java 服务，Grafana 统一展示。

## 0:40–2:10：任务业务闭环

打开 `http://localhost:8080`：

1. 注册或登录，展示 JWT 认证。
2. 创建项目和任务，说明 `X-Idempotency-Key`。
3. 依次启动、阻塞、恢复、完成任务。
4. 查看 Task Activity，说明领域状态机和审计时间线。
5. 请求一个高风险操作，展示申请、审批、一次性消费和事务回滚边界。

可以直接运行自动演示：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1
```

脚本会执行 9 个阶段，并同时校验 Core 审计与 Kafka 事件读模型。

## 2:10–3:20：Kafka 与可靠性

重点展示：

- `task_event_outbox` 与任务在同一 MySQL 事务中提交。
- Kafka Topic 使用任务 ID 作为 Key，保证同一任务分区内有序。
- Event Service 使用 `event_id` 主键去重。
- 失败消费固定重试 2 次，随后投递 `agentflow.task.lifecycle.v1.DLT`。
- `GET /api/v1/events` 返回独立数据库中的消费结果，而不是直接查询 Core 表。

## 3:20–4:15：Agent 工程化

从 `TaskAgentService` 与 Agent Run 页面说明：

- 显式控制 `Model → Tool → Result` 循环，而非把执行权完全交给框架。
- 限制模型次数、工具次数、总时长和重复调用。
- Tool 只能调用已有领域服务，无法绕过任务规则与权限边界。
- Run/Step 增量落库，失败时仍保留定位证据。
- 高风险副作用必须经过 Approval Gate。

## 4:15–5:00：质量证据与边界

依次展示：

1. GitHub Actions 的绿色 CI 状态。
2. `v3.0.1` Release 与三个服务 JAR。
3. `docs/E2E_VALIDATION_V3.md` 中的真实 Docker 验证记录。
4. Prometheus Targets：Gateway、Core、Event 均为 `up`。

最后明确项目边界：已经完成本地容器端到端验证，但不虚构生产流量；生产化仍需 Kafka 高可用、TLS/SASL、密钥管理、集中日志和容量测试。

## 高频追问准备

| 问题 | 回答主线 |
|---|---|
| 为什么不用“数据库保存后直接发 Kafka” | 数据库事务与 Kafka 不具备原子性；Outbox 把可靠重试窗口落到数据库中 |
| Outbox 会不会重复发送 | 会按至少一次语义设计，因此消费者必须以 `event_id` 幂等 |
| 为什么只拆 Event Service | 小规模、可验证的渐进式拆分；先隔离异步审计读模型，避免为拆分而拆分 |
| Agent 如何保证不乱改数据 | Tool 白名单、领域服务复用、RBAC、审批门禁、预算和审计共同约束 |
| Redis 挂了是否丢业务事实 | MySQL 承担最终事实；Redis 用于缓存、限流、Memory 与向量能力，不替代事务数据 |
| 你如何证明项目可以交付 | 19 项测试、9 步 Smoke Test、Compose 健康探针、E2E 证据、CI 与版本化 Release |
