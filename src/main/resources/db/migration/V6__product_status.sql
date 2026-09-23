CREATE TABLE product_statuses (
    uri       VARCHAR(500) PRIMARY KEY,
    code      VARCHAR(255),
    label     JSONB
);
CREATE INDEX idx_product_statuses_code ON product_statuses (code);
