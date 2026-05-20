CREATE TABLE geonames_fylker (
    geoname_id VARCHAR(20) PRIMARY KEY,
    uri        VARCHAR(500),
    name       VARCHAR(500)
);
CREATE INDEX idx_geonames_fylker_geoname_id ON geonames_fylker (geoname_id);

CREATE TABLE geonames_kommuner (
    geoname_id        VARCHAR(20) PRIMARY KEY,
    uri               VARCHAR(500),
    name              VARCHAR(500),
    fylke_geoname_id  VARCHAR(20)
);
CREATE INDEX idx_geonames_kommuner_geoname_id ON geonames_kommuner (geoname_id);
CREATE INDEX idx_geonames_kommuner_fylke_id  ON geonames_kommuner (fylke_geoname_id);
