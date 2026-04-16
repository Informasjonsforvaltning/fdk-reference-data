package no.fdk.referencedata.eu.datatheme;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "data_themes")
public class DataTheme {
    @Id
    @Column(name = "uri")
    String uri;

    @Column(name = "code")
    String code;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "label", columnDefinition = "jsonb")
    Map<String, String> label;

    @Column(name = "start_use")
    LocalDate startUse;

    @Column(name = "concept_schema_uri")
    String conceptSchemaUri;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "concept_schema_label", columnDefinition = "jsonb")
    Map<String, String> conceptSchemaLabel;

    @Column(name = "concept_schema_version")
    String conceptSchemaVersion;

    @Transient
    public ConceptSchema getConceptSchema() {
        if (conceptSchemaUri == null && conceptSchemaLabel == null && conceptSchemaVersion == null) {
            return null;
        }
        return ConceptSchema.builder()
                .uri(conceptSchemaUri)
                .label(conceptSchemaLabel)
                .versionNumber(conceptSchemaVersion)
                .build();
    }

    @Transient
    public void setConceptSchema(ConceptSchema cs) {
        if (cs != null) {
            this.conceptSchemaUri = cs.getUri();
            this.conceptSchemaLabel = cs.getLabel();
            this.conceptSchemaVersion = cs.getVersionNumber();
        } else {
            this.conceptSchemaUri = null;
            this.conceptSchemaLabel = null;
            this.conceptSchemaVersion = null;
        }
    }
}
