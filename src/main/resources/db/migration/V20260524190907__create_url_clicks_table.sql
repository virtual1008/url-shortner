-- V1__create_url_clicks_table.sql

CREATE TABLE url_clicks (
    id BIGSERIAL PRIMARY KEY,
    short_code VARCHAR(255) NOT NULL,
    ip_address VARCHAR(255),
    click_timestamp TIMESTAMP NOT NULL
);

-- Index to drastically speed up analytics queries by short code
CREATE INDEX idx_url_clicks_short_code ON url_clicks(short_code);