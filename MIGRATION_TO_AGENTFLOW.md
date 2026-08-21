# 迁移到 AgentFlow Enterprise 2.0

建议保留原项目作为只读备份，再在新目录验证本版本。

```powershell
Copy-Item "E:\cjl\job\project\ai\gpt\java-ai-agent\java-ai-agent-enterprise" `
          "E:\cjl\job\project\ai\gpt\java-ai-agent\java-ai-agent-enterprise-backup" `
          -Recurse
```

将本项目解压到：

```text
E:\cjl\job\project\ai\gpt\java-ai-agent\agentflow-enterprise
```

迁移步骤：

1. 使用 IntelliJ IDEA 打开 `pom.xml`，Project SDK 选择 Java 21。
2. 复制 `.env.example` 为 `.env` 并替换所有密钥和密码。
3. 对已有数据库先备份，再执行 Flyway；`V2__agentflow_project_and_audit.sql` 会新增项目、任务审计和审批字段。
4. 执行：

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd clean package
```

5. 启动 MySQL 和 Redis：

```powershell
docker compose up -d mysql redis
```

6. 启动应用并按照 `api-examples.http` 验证项目、任务、Agent、审批、RAG 和评估。

不要直接在生产数据库执行迁移。先在本地副本或测试库验证字段、索引、外键和旧数据兼容性。
