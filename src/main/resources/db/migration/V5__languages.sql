CREATE TABLE languages (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_languages_code ON languages (code);
