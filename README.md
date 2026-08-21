# AgentFlow Enterprise

<p align="center"><img src="docs/assets/hero.png" alt="AgentFlow Enterprise" width="100%"></p>

<p align="center">
  <a href="https://github.com/pravinkaveya473-sketch/agentflow-enterprise/actions"><img src="https://img.shields.io/github/actions/workflow/status/pravinkaveya473-sketch/agentflow-enterprise/ci.yml?branch=main&style=flat-square&label=build" alt="Build"></a>
  <img src="https://img.shields.io/badge/release-2.0.0-53e6d1?style=flat-square" alt="Release 2.0.0">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/tests-16%20passed-22C55E?style=flat-square" alt="16 tests passed">
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-6698ff?style=flat-square" alt="MIT License"></a>
</p>

<p align="center"><strong>企业级 Java AI Agent 任务编排、安全审批与可审计执行平台</strong></p>

AgentFlow 将项目协作、任务状态机、显式 Agent Loop、人类审批、权限感知 RAG、Memory 和全链路观测组合为一个可运行系统。它解决的核心问题不是“调用一次模型”，而是：当 Agent 能修改真实业务状态时，怎样限制预算、阻断越权、保存证据并让故障可以追溯。

维护者：[@pravinkaveya473-sketch](https://github.com/pravinkaveya473-sketch) · 当前稳定版：`2.0.0`

[快速开始](#快速开始) · [5 分钟演示](docs/DEMO.md) · [API 参考](docs/API.md) · [架构设计](docs/ARCHITECTURE.md) · [运维手册](docs/OPERATIONS.md) · [版本记录](CHANGELOG.md)

## 产品界面

应用内置响应式 Operations Console。登录后可统一查看项目、任务状态分布、待审批操作和最近 Agent Run，不需要额外部署前端工程。

<p align="center"><img src="docs/assets/operations-console.png" alt="AgentFlow Operations Console login" width="100%"></p>

<p align="center"><img src="docs/assets/demo.gif" alt="AgentFlow controlled Agent workflow" width="100%"></p>

## 核心价值

| 生产问题 | AgentFlow 的实现 |
|---|---|
| 模型如何稳定完成多步任务 | 应用显式控制 Model → Tool → Result 循环；限制模型次数、工具次数、总时长和重复调用 |
| Agent 如何安全修改业务数据 | JWT + RBAC + 领域状态机；高风险删除必须通过审批并在同一事务中消费审批 |
| 多用户知识问答如何隔离 | 文档、向量过滤、Memory 会话均使用用户/知识库作用域；低相似度时拒绝编造 |
| 如何定位一次错误执行 | Run/Step、Task Activity、Approval、Trace ID 和 Micrometer 指标形成完整证据链 |
| 怎样证明仓库可以交付 | 16 项自动化测试、Compose 环境、Smoke Test、CI、Dependabot 和标签发布流程 |

## 架构

```mermaid
flowchart LR
    UI[Operations Console / API Client] --> SEC[JWT + RBAC]
    SEC --> APP[Project & Task Services]
    SEC --> AGENT[Explicit Agent Runtime]
    AGENT --> MODEL[OpenAI-compatible LLM]
    AGENT --> TOOLS[Project / Task / Knowledge Tools]
    TOOLS --> GATE[Approval Gate]
    APP --> MYSQL[(MySQL)]
    GATE --> MYSQL
    AGENT --> REDIS[(Redis Memory / Cache / Vector)]
    AGENT -. Run & Step evidence .-> MYSQL
    APP -. metrics & traces .-> OBS[Prometheus / Grafana]
```

### 执行不变量

- 模型不能直接访问数据库，只能调用已注册、受业务规则约束的 Java Tool。
- 工具执行前必须保留最终回答所需的模型调用预算，避免“副作用已发生但无最终结果”。
- 项目归档后不能创建任务；终态任务不能再次修改；阻塞任务必须包含原因。
- 审批只能被消费一次，并受状态、过期时间、资源 ID 和乐观锁共同保护。
- 用户只能访问自己拥有或被分配的任务、知识库和 Agent Run。

## 能力地图

### 协作域

- 项目创建、编辑、归档、恢复与用户级幂等键
- 项目内/独立任务、负责人、优先级、截止时间和状态筛选
- `CREATED → IN_PROGRESS → COMPLETED`、阻塞/恢复、取消和活动时间线
- JPA 乐观锁和数据库唯一约束处理并发更新与重复请求

### Agent Runtime

- 手动 Tool Calling 循环和项目/任务/知识工具注册
- 模型调用、工具调用、执行时间和重复调用保护
- Agent Run/Step 增量持久化、结果缓存、限流和会话 Memory
- JSONL 评估集与工具序列自动评估

### 安全与知识

- HS256 JWT、issuer 校验、`USER / APPROVER / ADMIN` RBAC
- 高风险操作申请、批准、拒绝、过期和一次性消费
- PDF、Word、TXT、Markdown 解析，扩展名/大小/字符数约束和 SHA-256 去重
- Redis VectorStore Top-K、相似度阈值、引用信息和租户过滤

### 可观测与交付

- Trace ID、Actuator、Prometheus 指标和 Grafana 数据源
- 内置 Operations Console 与聚合 Dashboard API
- Dockerfile、Docker Compose、健康探针、Smoke Test 和 HTTP 示例
- GitHub Actions CI、Dependabot 和 tag-based Release

## 技术栈

| Layer | Technology |
|---|---|
| Runtime | Java 21, Spring Boot 4.1, Spring AI 2.0 |
| Security | Spring Security, OAuth2 Resource Server, JWT |
| Persistence | Spring Data JPA, Flyway, MySQL 8.4 |
| AI data | Redis Stack VectorStore, Redis Chat Memory, Apache Tika |
| Operations | Actuator, Micrometer, Prometheus, Grafana, Docker Compose |
| Verification | JUnit 5, Mockito, Maven Wrapper, GitHub Actions |

## 快速开始

### 前置条件

- JDK 21
- Docker Desktop / Docker Engine with Compose
- DashScope 或其他兼容当前 OpenAI 配置的模型凭据

### 一条命令启动

```powershell
Copy-Item .env.example .env
# 将所有 replace-with-* 替换为本地安全值
docker compose up -d --build
```

打开：

- Operations Console: `http://localhost:8080`
- API: `http://localhost:8080/api/v1`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

首次启动会根据环境变量创建管理员账户。不要在公共环境沿用示例密码。

### 本地开发

```powershell
docker compose up -d mysql redis
$env:DASHSCOPE_API_KEY="your-key"
$env:JWT_SECRET="a-random-secret-with-at-least-32-bytes"
$env:MYSQL_PASSWORD="your-mysql-password"
$env:REDIS_PASSWORD="your-redis-password"
$env:ADMIN_PASSWORD="your-admin-password"
.\mvnw.cmd spring-boot:run
```

## 验证

```powershell
.\mvnw.cmd clean verify
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1
```

当前测试覆盖领域状态迁移、审批生命周期、知识文件策略、Agent 运行上下文、任务服务权限与活动记录，以及 Dashboard 聚合逻辑。CI 在每次 push 和 pull request 上运行；发布标签会额外生成可下载 JAR。

## 目录

```text
src/main/java/com/cjl/java_ai_agent
├── agent          # 显式 Agent Loop 与 Run/Step 证据
├── approval       # 高风险操作审批门禁
├── dashboard      # Operations Console 聚合 API
├── evaluation     # Agent 评估数据集和执行器
├── infrastructure # Redis 幂等、限流和缓存
├── knowledge      # 文档摄取、向量检索和 RAG
├── project        # 项目聚合与 Agent Tool
├── security       # JWT、RBAC 和用户管理
└── task           # 任务状态机、活动审计与 Tool
```

## 适用边界

这是一个可构建、可测试、可本地部署的工程作品，不声称已经承载真实生产流量。正式生产使用前仍应接入企业密钥管理、TLS、数据库备份、集中日志、告警规则与组织级身份源，并根据实际流量完成容量测试。

## License

[MIT](LICENSE) © [pravinkaveya473-sketch](https://github.com/pravinkaveya473-sketch)
