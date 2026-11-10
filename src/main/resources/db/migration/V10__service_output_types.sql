CREATE TABLE service_output_types (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_service_output_types_code ON service_output_types (code);
