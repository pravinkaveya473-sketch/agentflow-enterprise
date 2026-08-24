CREATE TABLE app_user (
    id VARCHAR(64) PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_app_user_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE task (
    id VARCHAR(64) PRIMARY KEY,
    owner_username VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    deadline DATETIME(6) NULL,
    priority VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    idempotency_key VARCHAR(128) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_task_idempotency UNIQUE (owner_username, idempotency_key),
    INDEX idx_task_owner_status (owner_username, status),
    INDEX idx_task_deadline (deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE agent_run (
    id VARCHAR(64) PRIMARY KEY,
    conversation_id VARCHAR(128) NOT NULL,
    username VARCHAR(64) NOT NULL,
    user_message TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    final_content MEDIUMTEXT NULL,
    error_message TEXT NULL,
    model_call_count INT NOT NULL DEFAULT 0,
    tool_call_count INT NOT NULL DEFAULT 0,
    idempotency_key VARCHAR(128) NULL,
    started_at TIMESTAMP(6) NOT NULL,
    completed_at TIMESTAMP(6) NULL,
    CONSTRAINT uk_agent_run_idempotency UNIQUE (username, idempotency_key),
    INDEX idx_agent_run_user_started (username, started_at),
    INDEX idx_agent_run_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE agent_step (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    run_id VARCHAR(64) NOT NULL,
    sequence_no INT NOT NULL,
    step_type VARCHAR(32) NOT NULL,
    tool_call_id VARCHAR(128) NULL,
    tool_name VARCHAR(128) NULL,
    content MEDIUMTEXT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_agent_step_run FOREIGN KEY (run_id) REFERENCES agent_run(id) ON DELETE CASCADE,
    CONSTRAINT uk_agent_step_sequence UNIQUE (run_id, sequence_no),
    INDEX idx_agent_step_tool (tool_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE approval_request (
    id VARCHAR(64) PRIMARY KEY,
    run_id VARCHAR(64) NULL,
    action_type VARCHAR(64) NOT NULL,
    resource_id VARCHAR(128) NULL,
    requested_by VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    payload_json MEDIUMTEXT NULL,
    decided_by VARCHAR(64) NULL,
    decision_comment VARCHAR(500) NULL,
    requested_at TIMESTAMP(6) NOT NULL,
    decided_at TIMESTAMP(6) NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    INDEX idx_approval_status_requested (status, requested_at),
    INDEX idx_approval_requester (requested_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE knowledge_document (
    id VARCHAR(64) PRIMARY KEY,
    knowledge_base VARCHAR(128) NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(128) NULL,
    file_size BIGINT NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,
    sha256 VARCHAR(64) NOT NULL,
    chunk_count INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL,
    uploaded_by VARCHAR(64) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_knowledge_sha_tenant UNIQUE (tenant_id, knowledge_base, sha256),
    INDEX idx_knowledge_base (tenant_id, knowledge_base, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE evaluation_run (
    id VARCHAR(64) PRIMARY KEY,
    evaluation_type VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    total_cases INT NOT NULL,
    passed_cases INT NOT NULL,
    pass_rate DECIMAL(8,4) NOT NULL,
    result_json MEDIUMTEXT NOT NULL,
    started_by VARCHAR(64) NOT NULL,
    started_at TIMESTAMP(6) NOT NULL,
    completed_at TIMESTAMP(6) NOT NULL,
    INDEX idx_evaluation_started (evaluation_type, started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
