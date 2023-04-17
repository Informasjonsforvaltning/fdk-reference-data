package no.fdk.referencedata.los;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Data
@Document
public class LosNode {
    static final String RDFS_URI = "http://www.w3.org/2000/01/rdf-schema#";
    static final String NODE_IS_TEMA_OR_SUBTEMA = "https://psi.norge.no/los/ontologi/tema";
    static final String NODE_IS_EMNE = "https://psi.norge.no/los/ontologi/ord";

    public transient Long internalId;

    public List<URI> children;
    public List<URI> parents;
    public boolean isTheme;
    public List<String> losPaths;
    private Map<String, String> name;
    private Map<String, String> definition;
    @Id
    private String uri;
    private List<String> synonyms;
    private List<URI> relatedTerms;

}
