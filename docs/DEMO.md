# AgentFlow Enterprise：5 分钟面试演示

## 演示目标

用一条“创建项目 → 创建任务 → Agent 推进 → 高风险删除审批 → 审计追踪”的链路，说明系统不仅能调用模型，还能把 Agent 放进企业业务与安全边界。

## 1. 先证明工程可构建

```powershell
.\mvnw.cmd test
```

预期：16 项自动化测试通过。

## 2. 启动基础设施与应用

```powershell
Copy-Item .env.example .env
# 填写 .env 中的必填项
docker compose up -d --build
```

## 3. 展示业务闭环

先打开 `http://localhost:8080` 展示内置 Operations Console，再使用 `api-examples.http` 依次演示：

1. 注册并登录，获取 JWT。
2. 创建企业项目和任务。
3. 启动任务并查看 `task_activity`。
4. 请求删除任务，展示审批状态。
5. 使用 APPROVER/ADMIN 批准并执行删除。
6. 返回控制台查看状态分布和最近 Agent Run，再展示 Run/Step、Prometheus 指标和 Trace ID。

## 4. 面试讲解要点

- 为什么使用显式 Agent Loop，而不是完全交给框架自动执行。
- 为什么审批消费和业务删除必须在同一数据库事务中。
- Redis 在幂等、限流、运行缓存、Memory 和向量检索中的职责边界。
- 如何通过步骤预算、重复工具检测和运行审计控制 Agent 失控风险。

## 演示素材说明

README 中的 GIF 是项目架构与能力导览；真实端到端运行需要 MySQL、Redis 和有效模型 API Key。仓库不包含任何真实凭据。
