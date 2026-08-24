CREATE TABLE task_event_record (
    event_id VARCHAR(64) PRIMARY KEY,
    event_type VARCHAR(64) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    owner_username VARCHAR(64) NOT NULL,
    project_id VARCHAR(64) NULL,
    task_name VARCHAR(255) NOT NULL,
    before_status VARCHAR(32) NULL,
    after_status VARCHAR(32) NULL,
    aggregate_version BIGINT NOT NULL,
    occurred_at TIMESTAMP(6) NOT NULL,
    consumed_at TIMESTAMP(6) NOT NULL,
    INDEX idx_event_owner_occurred (owner_username, occurred_at),
    INDEX idx_event_aggregate_version (aggregate_id, aggregate_version),
    INDEX idx_event_type_occurred (event_type, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
