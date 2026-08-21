# Security Policy

## Reporting a vulnerability

请不要在公开 Issue 中披露可利用细节、真实凭据或生产数据。请通过 GitHub Security Advisory 的私密报告入口提交：

`https://github.com/pravinkaveya473-sketch/agentflow-enterprise/security/advisories/new`

报告建议包括影响版本、复现步骤、影响范围和修复建议。维护者会优先确认认证绕过、越权访问、审批绕过、敏感信息泄露和任意命令执行问题。

## Secret handling

- 仓库只提供 `.env.example`，真实 `.env` 不进入版本控制。
- API Key、JWT Secret、数据库密码和管理员密码必须通过环境变量注入。
- 示例凭据仅用于本地演示，不得复用于任何真实环境。
