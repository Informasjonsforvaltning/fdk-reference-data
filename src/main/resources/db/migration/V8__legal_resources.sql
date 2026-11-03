CREATE TABLE legal_resources
(
    uri        VARCHAR(500) PRIMARY KEY,
    identifier VARCHAR(100),
    label      JSONB,
    type       VARCHAR(50)
);

CREATE INDEX legal_resources_type_idx ON legal_resources (type);
