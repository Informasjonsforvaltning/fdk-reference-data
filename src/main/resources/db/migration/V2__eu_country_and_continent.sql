CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE countries (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_countries_code ON countries (code);
CREATE INDEX idx_countries_label_trgm ON countries USING GIN ((label::text) gin_trgm_ops);

CREATE TABLE continents (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_continents_code ON continents (code);
CREATE INDEX idx_continents_label_trgm ON continents USING GIN ((label::text) gin_trgm_ops);
