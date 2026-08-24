# Contributing

1. 从 `main` 创建功能分支。
2. 保持业务状态机、安全策略和审计语义向后兼容。
3. 新增行为必须补充测试；提交前执行 `.\mvnw.cmd clean verify` 与 `docker compose config --quiet`。
4. 不提交 `.env`、API Key、运行日志、数据库文件或 IDE 私有配置。
5. 涉及跨服务事件时，同时验证事件契约、Outbox 映射、消费幂等和 DLT 行为。
6. Pull Request 中说明问题、方案、风险、验证命令和回滚方式。
