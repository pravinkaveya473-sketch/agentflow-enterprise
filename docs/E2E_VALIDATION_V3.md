# AgentFlow 3.0.1 Docker end-to-end validation

- Validation date: 2026-08-24
- Environment: Windows, Docker Desktop Engine 29.6.2, Java 21
- Compose project: `agentflow-v3-e2e`

## Scope

This is a real container and business-flow validation, not a Compose syntax-only claim. A clean, isolated Compose project was used so the previous MySQL volume and its credentials remained untouched and available for rollback.

## Verified deployment

| Component | Result |
|---|---|
| Gateway Service | Healthy; readiness `UP`; host port `8080` |
| Core Service | Healthy; Flyway write schema at v3 |
| Event Service | Healthy; Flyway read-model schema at v1 |
| MySQL 8.4 | Healthy; bootstrap job exited `0` |
| Redis Stack 7.4 | Healthy |
| Kafka 4.1 KRaft | Healthy |
| Prometheus 3.5 | Running; Gateway/Core/Event targets all `up` |
| Grafana 12.1 | Running; database health `ok` |

## Business-path evidence

The test registered a new user, obtained a JWT through Gateway, and created a high-priority task with an idempotency key. Core Service committed the task and its Outbox record in one database transaction. The publisher sent the event to Kafka, Event Service consumed it into its independent read model, and the event API returned the matching aggregate ID.

- Created task ID: `78c0ee1a-a51c-41e0-985d-e2f66f2d60f0`
- Consumed event type: `TASK_CREATED`
- Event aggregate ID matched the task ID
- Latest Outbox record had `published_at` populated
- Kafka topics present: `agentflow.task.lifecycle.v1` and `agentflow.task.lifecycle.v1.DLT`
- Gateway readiness returned `UP`

The generated test account used only local disposable data. No production credential or private token is recorded here.

## Reproduction

```powershell
Copy-Item .env.example .env
# Replace secrets. A real model key is only required for AI/vector operations.
.\mvnw.cmd clean verify
.\mvnw.cmd clean package -DskipTests
docker compose -p agentflow-v3-e2e `
  -f compose.yaml -f compose.runtime.yaml `
  --env-file .env up -d --build
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1
```

For a real embedding credential, set `VECTOR_STORE_INITIALIZE_SCHEMA=true`. Keep it `false` when validating only the infrastructure and Kafka task lifecycle with the placeholder key.

When reusing an existing MySQL volume, keep the credentials that initialized that volume. Changing Compose environment variables does not rotate accounts already stored by MySQL.
