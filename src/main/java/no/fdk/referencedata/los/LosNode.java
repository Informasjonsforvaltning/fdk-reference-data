package no.fdk.referencedata.los;

import lombok.Data;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Data
public class LosNode {
    static final String RDFS_URI = "http://www.w3.org/2000/01/rdf-schema#";
    static final String NODE_IS_TEMA_OR_SUBTEMA = "https://psi.norge.no/los/ontologi/tema";
    static final String NODE_IS_EMNE = "https://psi.norge.no/los/ontologi/ord";

    public transient Long internalId;

    public List<URI> children;
    public List<URI> parents;
    public boolean isTema;
    public List<String> losPaths;
    private Map<String, String> name;
    private Map<String, String> definition;
    private String uri;
    private List<String> synonyms;
    private List<URI> relatedTerms;

}
