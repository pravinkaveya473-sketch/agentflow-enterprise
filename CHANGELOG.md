# Changelog

All notable changes are recorded here. This project follows semantic versioning.

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
