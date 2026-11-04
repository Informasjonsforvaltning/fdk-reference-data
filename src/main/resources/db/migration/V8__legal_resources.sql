CREATE TABLE legal_resources
(
    uri        VARCHAR(500) PRIMARY KEY,
    identifier VARCHAR(100),
    label      JSONB,
    type       VARCHAR(50)
);
