package no.fdk.referencedata.eu.eurovoc;

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

import java.net.URI;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "eurovocs")
public class EuroVoc {
    @Id
    @Column(name = "uri")
    String uri;

    @Column(name = "code")
    String code;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "label", columnDefinition = "jsonb")
    Map<String, String> label;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "children", columnDefinition = "jsonb")
    List<URI> children;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parents", columnDefinition = "jsonb")
    List<URI> parents;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "eurovoc_paths", columnDefinition = "jsonb")
    List<String> eurovocPaths;
}
