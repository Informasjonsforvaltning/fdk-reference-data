package no.fdk.referencedata.lovdata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "legal_resources")
public class LegalResource {
    @Id
    @Column(name = "uri")
    String uri;

    @Column(name = "identifier")
    String identifier;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "label", columnDefinition = "jsonb")
    Map<String, String> label;

    @Column(name = "type")
    String type;
}
