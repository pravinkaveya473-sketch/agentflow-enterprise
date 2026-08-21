# Operations guide

## Deployment model

The application is stateless at the HTTP layer. MySQL stores business/audit state; Redis provides rate limiting, idempotency, run cache, chat memory and vector search. Horizontal application replicas can share both services.

## Required secrets

- `DASHSCOPE_API_KEY`
- `JWT_SECRET` (at least 32 bytes)
- `MYSQL_PASSWORD`
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

The supplied Compose stack provisions Prometheus and a Grafana data source. Application metrics include Agent duration, run outcome, model calls and tool calls.

## Backup and recovery

Back up MySQL and the configured upload directory together. Redis chat/cache data is recoverable, but vector indexes should be included when re-ingestion time matters. Test database restore and document re-indexing before production use.

## Release verification

1. Run `./mvnw clean verify`.
2. Run `docker compose config` and confirm all required variables resolve.
3. Start the stack and execute `scripts/smoke-test.ps1`.
4. Verify readiness and Prometheus scraping.
5. Tag `vX.Y.Z`; the release workflow publishes the verified JAR.
