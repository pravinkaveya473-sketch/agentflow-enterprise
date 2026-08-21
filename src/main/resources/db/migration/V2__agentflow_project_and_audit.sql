CREATE TABLE project (
    id VARCHAR(64) PRIMARY KEY,
    owner_username VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(1000) NULL,
    status VARCHAR(32) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    idempotency_key VARCHAR(128) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_project_idempotency UNIQUE (owner_username, idempotency_key),
    INDEX idx_project_owner_status (owner_username, status),
    INDEX idx_project_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE task
    ADD COLUMN project_id VARCHAR(64) NULL AFTER owner_username,
    ADD COLUMN description VARCHAR(2000) NULL AFTER name,
    ADD COLUMN assignee_username VARCHAR(64) NULL AFTER description,
    ADD COLUMN blocked_reason VARCHAR(500) NULL AFTER status,
    ADD INDEX idx_task_project_status (project_id, status),
    ADD INDEX idx_task_assignee_status (assignee_username, status),
    ADD CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE SET NULL;

UPDATE task
SET assignee_username = owner_username
WHERE assignee_username IS NULL;

CREATE TABLE task_activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(64) NOT NULL,
    actor VARCHAR(64) NOT NULL,
    activity_type VARCHAR(32) NOT NULL,
    before_status VARCHAR(32) NULL,
    after_status VARCHAR(32) NULL,
    details VARCHAR(1000) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    INDEX idx_task_activity_task_created (task_id, created_at),
    INDEX idx_task_activity_actor_created (actor, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE agent_run
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE approval_request
    ADD COLUMN consumed_at TIMESTAMP(6) NULL AFTER decided_at,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD INDEX idx_approval_expiry (status, expires_at);
