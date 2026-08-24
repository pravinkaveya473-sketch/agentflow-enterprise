# Operations guide

## Deployment model

Gateway is the only public HTTP entry point. Core Service owns the `agentflow` write model; Event Service owns the `agentflow_event` read model; Kafka carries task lifecycle events; Redis provides rate limiting, idempotency, run cache, chat memory and vector search.

## Required secrets

- `DASHSCOPE_API_KEY`
- `JWT_SECRET` (at least 32 bytes)
- `MYSQL_PASSWORD`
- `MYSQL_ROOT_PASSWORD` (Compose database bootstrap only)
- `REDIS_PASSWORD`
- `ADMIN_PASSWORD`
- `GRAFANA_ADMIN_PASSWORD` when Grafana is enabled

Never place real values in `.env.example`, source code, container images or CI logs.

## Health and observability

| Endpoint | Audience |
|---|---|
| `/actuator/health/liveness` | Container liveness probe |
| `/actuator/health/readiness` | Load balancer readiness probe |
| `/actuator/prometheus` | Prometheus scraper |
| `/actuator/metrics/**` | Administrators only |

The supplied Compose stack provisions Prometheus and a Grafana data source. Prometheus scrapes Gateway, Core and Event Service separately. Core metrics include Agent duration, run outcome, model calls and tool calls.

## Kafka operations

- Business commits and Outbox inserts are atomic in Core MySQL.
- Unpublished rows are retried by the Outbox scheduler; inspect `attempt_count` and `last_error` when Kafka is unavailable.
- Event Service retries a failed record twice and then publishes it to `agentflow.task.lifecycle.v1.DLT`.
- Local Compose uses one KRaft node for reproducibility. Production should use a multi-controller quorum and appropriate authentication/TLS.

## Backup and recovery

Back up both MySQL databases and the configured upload directory together. Preserve Kafka retention long enough to rebuild the Event Service read model. Redis chat/cache data is recoverable, but vector indexes should be included when re-ingestion time matters. See [`UPGRADE_ROLLBACK_V3.md`](UPGRADE_ROLLBACK_V3.md).

## Release verification

1. Run `./mvnw clean verify`.
2. Run `docker compose config` and confirm all required variables resolve.
3. Start the stack and execute `scripts/smoke-test.ps1`.
4. Verify readiness and Prometheus scraping.
5. Tag `vX.Y.Z`; the release workflow publishes all three verified service JARs.
