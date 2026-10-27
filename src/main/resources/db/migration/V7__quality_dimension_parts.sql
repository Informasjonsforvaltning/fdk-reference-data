ALTER TABLE quality_dimensions
    ADD COLUMN has_parts JSONB,
    ADD COLUMN is_part_of VARCHAR(500);
