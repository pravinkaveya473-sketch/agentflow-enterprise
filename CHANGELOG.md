# Changelog

All notable changes are recorded here. This project follows semantic versioning.

## 3.0.1 — 2026-08-24

- Completed a clean-volume Docker Compose end-to-end deployment and business-path verification.
- Fixed Spring Boot 4 Kafka auto-configuration by adopting the Kafka starter and Jackson 2 compatibility module required by Spring Kafka serializers.
- Added the Jedis client required by Spring AI Redis VectorStore and made local vector-index initialization configurable so infrastructure tests do not require a live embedding credential.
- Removed package-manager dependence from runtime images, added BuildKit/Maven caching, and provided a host-built JAR runtime-image overlay for restricted networks.
- Verified transactional Outbox publication, Kafka consumption, DLT provisioning, Prometheus targets and Grafana health.

## 3.0.0 — 2026-08-24

- Converted the repository into a Maven multi-module Spring Cloud architecture.
- Added Spring Cloud Gateway as the single public API entry point.
- Extracted an Event Service with an independent MySQL read model and JWT boundary.
- Added Kafka task lifecycle events using a transactional Outbox in Core Service.
- Added idempotent event consumption, bounded retry and dead-letter topic routing.
- Added a KRaft Kafka broker and multi-service health checks to Docker Compose.
- Preserved the complete 2.0 modular-monolith baseline under tag `v2.0.0` and branch `archive/v2.0.0-modular-monolith`.

## 2.0.0 — 2026-08-22

- Added a responsive operations console for projects, tasks, approvals and Agent traces.
- Added the authenticated dashboard aggregation API.
- Completed project and task lifecycle rules with activity evidence and optimistic locking.
- Added an explicit tool-calling Agent loop with run/step persistence, budgets and duplicate-call protection.
- Added approval gates, JWT/RBAC, tenant-scoped RAG, Redis memory, evaluation and observability.
- Added Docker Compose infrastructure, Prometheus/Grafana provisioning, CI and tag-based releases.
- Promoted the repository from phase documentation to a stable `2.0.0` portfolio release.
