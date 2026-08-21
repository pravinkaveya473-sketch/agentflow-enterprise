# API reference

All business responses use `{ success, code, message, data, timestamp }`. Except for authentication and health endpoints, requests require `Authorization: Bearer <token>`.

## Authentication

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Create a user and return a JWT |
| `POST` | `/api/v1/auth/login` | Authenticate and return a JWT |

## Operations console

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/dashboard/overview` | Project/task totals, task states, approval count and recent Agent runs |

## Projects and tasks

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/projects` | Create an idempotent project |
| `GET` | `/api/v1/projects` | List projects owned by the current user |
| `PATCH` | `/api/v1/projects/{id}` | Update an active project |
| `POST` | `/api/v1/projects/{id}/archive` | Archive a project |
| `POST` | `/api/v1/projects/{id}/restore` | Restore a project |
| `POST` | `/api/v1/tasks` | Create an idempotent task |
| `GET` | `/api/v1/tasks` | Filter accessible tasks by project/status |
| `PATCH` | `/api/v1/tasks/{id}` | Update a non-terminal task |
| `POST` | `/api/v1/tasks/{id}/start` | Move `CREATED → IN_PROGRESS` |
| `POST` | `/api/v1/tasks/{id}/block` | Block a task with a reason |
| `POST` | `/api/v1/tasks/{id}/resume` | Resume a blocked task |
| `POST` | `/api/v1/tasks/{id}/complete` | Complete an active task |
| `POST` | `/api/v1/tasks/{id}/cancel` | Cancel a non-terminal task |
| `GET` | `/api/v1/tasks/{id}/activities` | Read task lifecycle evidence |

## Agent, knowledge and approvals

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/agents/task/runs` | Execute the controlled task Agent loop |
| `GET` | `/api/v1/agents/task/runs/{runId}` | Read a run and its step trace |
| `POST` | `/api/v1/ai/chat` | General chat with tenant-scoped memory |
| `POST` | `/api/v1/knowledge/{base}/documents` | Parse and index a document |
| `GET` | `/api/v1/knowledge/{base}/documents` | List tenant knowledge documents |
| `POST` | `/api/v1/knowledge/{base}/ask` | RAG answer with citations |
| `GET` | `/api/v1/approvals/pending` | List pending approvals (`APPROVER/ADMIN`) |
| `POST` | `/api/v1/approvals/{id}/approve` | Approve a high-risk operation |
| `POST` | `/api/v1/approvals/{id}/reject` | Reject a high-risk operation |

Executable request examples are maintained in [`api-examples.http`](../api-examples.http).
