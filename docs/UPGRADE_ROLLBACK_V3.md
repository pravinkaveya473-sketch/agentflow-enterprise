# AgentFlow 2.0 → 3.0 升级与回退

## 可回退基线

- 2.0 固定标签：`v2.0.0`
- 2.0 归档分支：`archive/v2.0.0-modular-monolith`
- 3.0 开发分支：`feat/v3-spring-cloud-kafka`
- 3.0 发布标签：`v3.0.0`

标签指向不可变提交，归档分支用于 GitHub 页面直接浏览。不要强制移动 `v2.0.0`。

## 升级前备份

生产环境应使用平台提供的 MySQL 快照/备份能力，并在隔离环境验证恢复。不要把 SQL 备份或真实密码提交到 Git。

## 升级

```powershell
git fetch --all --tags
git switch main
git pull --ff-only
Copy-Item .env.example .env
# 填写 .env 中的真实密钥
docker compose up -d --build
docker compose ps
```

`mysql-bootstrap` 会幂等创建 Event Service 数据库，因此全新安装和复用 2.0 MySQL 数据卷的升级场景都能直接执行。

Core Service 的 V3 Flyway 迁移只新增 `task_event_outbox`；Event Service 使用新的 `agentflow_event` 数据库。原 2.0 表未被删除或重命名，因此数据库变更对代码回退保持向后兼容。

## 快速回退到 2.0

```powershell
docker compose down
git switch archive/v2.0.0-modular-monolith
Copy-Item .env.example .env
# 2.0 不需要 Kafka 与 agentflow_event
docker compose up -d --build
```

也可以从标签创建一次性修复分支：

```powershell
git switch -c hotfix/v2.0.0 v2.0.0
```

回退后，新增的 `task_event_outbox` 表和 `agentflow_event` 数据库可以暂时保留，不影响 2.0 运行。确认不再恢复 3.0 且已经完成备份后，才考虑人工清理。

## 恢复 3.0

```powershell
docker compose down
git switch main
docker compose up -d --build
```

未发布的 Outbox 记录会在 Core Service 恢复后继续投递；Event Service 以 `event_id` 去重，因此重复投递不会生成重复读模型。

## 验收检查

- Gateway、Core、Event、MySQL、Redis、Kafka 状态均为 healthy；
- `agentflow.task.lifecycle.v1` 和 `.DLT` topic 存在；
- 任务状态变化后 `task_event_outbox.published_at` 被填写；
- `/api/v1/events` 可以查询到当前用户的任务事件；
- Prometheus 能抓取三个 Java 服务的 `/actuator/prometheus`。
