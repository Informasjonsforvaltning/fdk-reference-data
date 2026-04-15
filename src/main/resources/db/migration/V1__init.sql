-- ============================================================
-- Infrastructure tables
-- ============================================================

CREATE TABLE rdf_sources (
    id      VARCHAR(500) PRIMARY KEY,
    turtle  TEXT
);

CREATE TABLE harvest_settings (
    id                   VARCHAR(500) PRIMARY KEY,
    latest_version       VARCHAR(500),
    latest_harvest_date  TIMESTAMP
);

-- ============================================================
-- Category A: Simple code-lookup tables (uri, code, label JSONB)
-- ============================================================

CREATE TABLE access_rights (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_access_rights_code ON access_rights (code);

CREATE TABLE concept_statuses (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_concept_statuses_code ON concept_statuses (code);

CREATE TABLE main_activities (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_main_activities_code ON main_activities (code);

CREATE TABLE high_value_categories (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_high_value_categories_code ON high_value_categories (code);

CREATE TABLE audience_types (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_audience_types_code ON audience_types (code);

CREATE TABLE evidence_types (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_evidence_types_code ON evidence_types (code);

CREATE TABLE legal_resource_types (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_legal_resource_types_code ON legal_resource_types (code);

CREATE TABLE quality_dimensions (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_quality_dimensions_code ON quality_dimensions (code);

CREATE TABLE relationship_with_source_types (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_rel_with_source_types_code ON relationship_with_source_types (code);

CREATE TABLE role_types (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_role_types_code ON role_types (code);

CREATE TABLE service_channel_types (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_service_channel_types_code ON service_channel_types (code);

CREATE TABLE publisher_types (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_publisher_types_code ON publisher_types (code);

CREATE TABLE adms_statuses (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_adms_statuses_code ON adms_statuses (code);

CREATE TABLE mobility_themes (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_mobility_themes_code ON mobility_themes (code);

CREATE TABLE mobility_data_standards (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_mobility_data_standards_code ON mobility_data_standards (code);

CREATE TABLE mobility_conditions (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_mobility_conditions_code ON mobility_conditions (code);

CREATE TABLE concept_subjects (
    uri   VARCHAR(500) PRIMARY KEY,
    code  VARCHAR(255),
    label JSONB
);
CREATE INDEX idx_concept_subjects_code ON concept_subjects (code);

-- ============================================================
-- Category B: Code-lookup with extra columns
-- ============================================================

CREATE TABLE frequencies (
    uri        VARCHAR(500) PRIMARY KEY,
    code       VARCHAR(255),
    label      JSONB,
    sort_index INTEGER
);
CREATE INDEX idx_frequencies_code ON frequencies (code);

CREATE TABLE currencies (
    uri       VARCHAR(500) PRIMARY KEY,
    code      VARCHAR(255),
    label     JSONB,
    start_use DATE
);
CREATE INDEX idx_currencies_code ON currencies (code);

CREATE TABLE dataset_types (
    uri       VARCHAR(500) PRIMARY KEY,
    code      VARCHAR(255),
    label     JSONB,
    start_use DATE
);
CREATE INDEX idx_dataset_types_code ON dataset_types (code);

CREATE TABLE distribution_statuses (
    uri       VARCHAR(500) PRIMARY KEY,
    code      VARCHAR(255),
    label     JSONB,
    start_use DATE
);
CREATE INDEX idx_distribution_statuses_code ON distribution_statuses (code);

CREATE TABLE distribution_types (
    uri       VARCHAR(500) PRIMARY KEY,
    code      VARCHAR(255),
    label     JSONB,
    start_use DATE
);
CREATE INDEX idx_distribution_types_code ON distribution_types (code);

CREATE TABLE planned_availabilities (
    uri       VARCHAR(500) PRIMARY KEY,
    code      VARCHAR(255),
    label     JSONB,
    start_use DATE
);
CREATE INDEX idx_planned_availabilities_code ON planned_availabilities (code);

CREATE TABLE data_themes (
    uri                    VARCHAR(500) PRIMARY KEY,
    code                   VARCHAR(255),
    label                  JSONB,
    start_use              DATE,
    concept_schema_uri     VARCHAR(500),
    concept_schema_label   JSONB,
    concept_schema_version VARCHAR(255)
);
CREATE INDEX idx_data_themes_code ON data_themes (code);

CREATE TABLE licences (
    uri        VARCHAR(500) PRIMARY KEY,
    code       VARCHAR(255),
    label      JSONB,
    definition JSONB,
    deprecated BOOLEAN,
    context    JSONB
);
CREATE INDEX idx_licences_code ON licences (code);

-- ============================================================
-- Category C: Non-standard structures
-- ============================================================

CREATE TABLE file_types (
    uri        VARCHAR(500) PRIMARY KEY,
    code       VARCHAR(255),
    media_type VARCHAR(500)
);
CREATE INDEX idx_file_types_code ON file_types (code);

CREATE TABLE media_types (
    uri      VARCHAR(500) PRIMARY KEY,
    name     VARCHAR(500),
    type     VARCHAR(255),
    sub_type VARCHAR(255)
);
CREATE INDEX idx_media_types_type ON media_types (type);
CREATE INDEX idx_media_types_sub_type ON media_types (sub_type);

CREATE TABLE kommune_organisasjoner (
    uri                  VARCHAR(500) PRIMARY KEY,
    organisasjonsnummer  VARCHAR(20),
    organisasjonsnavn    VARCHAR(500),
    kommunenavn          VARCHAR(500),
    kommunenummer        VARCHAR(10)
);
CREATE INDEX idx_kommune_org_nummer ON kommune_organisasjoner (organisasjonsnummer);
CREATE INDEX idx_kommune_kommunenr ON kommune_organisasjoner (kommunenummer);

CREATE TABLE fylke_organisasjoner (
    uri                  VARCHAR(500) PRIMARY KEY,
    organisasjonsnummer  VARCHAR(20),
    organisasjonsnavn    VARCHAR(500),
    fylkesnavn           VARCHAR(500),
    fylkesnummer         VARCHAR(10)
);
CREATE INDEX idx_fylke_org_nummer ON fylke_organisasjoner (organisasjonsnummer);
CREATE INDEX idx_fylke_fylkesnr ON fylke_organisasjoner (fylkesnummer);

CREATE TABLE enheter (
    uri  VARCHAR(500) PRIMARY KEY,
    name VARCHAR(500),
    code VARCHAR(50)
);
CREATE INDEX idx_enheter_code ON enheter (code);

CREATE TABLE enhet_varianter (
    uri  VARCHAR(500) PRIMARY KEY,
    name VARCHAR(500),
    code VARCHAR(50)
);
CREATE INDEX idx_enhet_varianter_code ON enhet_varianter (code);

-- ============================================================
-- Category D: Graph/tree entities
-- ============================================================

CREATE TABLE los_nodes (
    uri           VARCHAR(500) PRIMARY KEY,
    name          JSONB,
    definition    JSONB,
    is_theme      BOOLEAN NOT NULL DEFAULT FALSE,
    los_paths     JSONB,
    children      JSONB,
    parents       JSONB,
    synonyms      JSONB,
    related_terms JSONB
);

CREATE TABLE eurovocs (
    uri           VARCHAR(500) PRIMARY KEY,
    code          VARCHAR(255),
    label         JSONB,
    children      JSONB,
    parents       JSONB,
    eurovoc_paths JSONB
);
CREATE INDEX idx_eurovocs_code ON eurovocs (code);
