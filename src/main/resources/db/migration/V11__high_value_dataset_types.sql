CREATE TABLE high_value_dataset_types (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_high_value_dataset_types_code ON high_value_dataset_types (code);
