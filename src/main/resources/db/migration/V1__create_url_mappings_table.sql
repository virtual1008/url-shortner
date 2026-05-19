CREATE TABLE url_mappings (
    id BIGSERIAL PRIMARY KEY,

    short_code VARCHAR(20) UNIQUE,

    original_url TEXT NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    expires_at TIMESTAMP,

    deleted_at TIMESTAMP
);