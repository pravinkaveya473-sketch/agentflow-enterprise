# 构建与验证状态

## 已在交付环境完成

- `pom.xml` XML 解析通过
- `application.yaml`、`application-prod.yaml`、`compose.yaml`、Prometheus/Grafana YAML 解析通过
- Flyway `V1`/`V2` SQL 结构静态检查完成
- 评估 JSONL 逐行 JSON 解析通过
- JDK 21 `javac` 词法/语法扫描未发现括号、字符串、分号或语句结构错误
- Project、Task、Approval、TaskActivity 等领域模型使用临时 Jakarta 注解桩完成实际 `javac` 编译
- 配置属性与文件上传策略使用临时 Spring 接口桩完成实际 `javac` 编译
- 关键安全项检查：生产配置无弱口令回退；`.env` 已加入 `.gitignore`
- PowerShell 端到端冒烟脚本已提供：`scripts/smoke-test.ps1`

## 当前交付环境无法完成

- Maven 全量依赖解析和 `clean test`
- Spring ApplicationContext 启动
- MySQL、Redis Stack、DashScope 的真实端到端联调
- Docker Compose 容器启动验证

原因：当前容器没有 Maven/Docker，且无法解析 Maven Central 域名下载 Maven Wrapper 与依赖。此限制不等同于项目已通过完整构建。

## 请在联网 Windows 开发机执行

```powershell
Copy-Item .env.example .env
# 替换 .env 中全部 replace-with-* 值

.\mvnw.cmd clean test
.\mvnw.cmd clean package

docker compose up -d mysql redis
# 在 IDEA 或命令行启动应用后：
.\scripts\smoke-test.ps1
```

全量 Compose 验证：

```powershell
docker compose up -d --build
docker compose ps
.\scripts\smoke-test.ps1
```

只有上述测试通过并保存日志后，才适合在简历中写“完成端到端部署与验证”以及任何性能数字。
