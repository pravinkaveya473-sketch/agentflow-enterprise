CREATE TABLE task_event_outbox (
    event_id VARCHAR(64) PRIMARY KEY,
    event_type VARCHAR(64) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    owner_username VARCHAR(64) NOT NULL,
    project_id VARCHAR(64) NULL,
    task_name VARCHAR(255) NOT NULL,
    before_status VARCHAR(32) NULL,
    after_status VARCHAR(32) NULL,
    aggregate_version BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    published_at TIMESTAMP(6) NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    last_error VARCHAR(1000) NULL,
    INDEX idx_outbox_pending (published_at, created_at),
    INDEX idx_outbox_aggregate_version (aggregate_id, aggregate_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
