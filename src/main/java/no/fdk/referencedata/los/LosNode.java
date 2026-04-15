package no.fdk.referencedata.los;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "los_nodes")
public class LosNode {
    static final String RDFS_URI = "http://www.w3.org/2000/01/rdf-schema#";
    static final String NODE_IS_TEMA_OR_SUBTEMA = "https://psi.norge.no/los/ontologi/tema";
    static final String NODE_IS_EMNE = "https://psi.norge.no/los/ontologi/ord";

    @Transient
    public Long internalId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "children", columnDefinition = "jsonb")
    public List<URI> children;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parents", columnDefinition = "jsonb")
    public List<URI> parents;

    @Column(name = "is_theme")
    public boolean isTheme;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "los_paths", columnDefinition = "jsonb")
    public List<String> losPaths;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "name", columnDefinition = "jsonb")
    private Map<String, String> name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "definition", columnDefinition = "jsonb")
    private Map<String, String> definition;

    @Id
    @Column(name = "uri")
    private String uri;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "synonyms", columnDefinition = "jsonb")
    private List<String> synonyms;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "related_terms", columnDefinition = "jsonb")
    private List<URI> relatedTerms;

    public boolean getIsTheme() {
        return isTheme;
    }
}
