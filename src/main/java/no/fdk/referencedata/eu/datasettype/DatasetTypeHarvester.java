package no.fdk.referencedata.eu.datasettype;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.eu.vocabulary.EUAuthorityOntology;
import no.fdk.referencedata.eu.vocabulary.EUDatasetType;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DatasetTypeHarvester extends AbstractEuHarvester<DatasetType> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());
    private static String VERSION = "0";

    public DatasetTypeHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<DatasetType> harvest() {
        log.info("Starting harvest of EU dataset types");
        final org.springframework.core.io.Resource datasetTypeRdfSource = getSource();
        if(datasetTypeRdfSource == null) {
            return Flux.error(new Exception("Unable to fetch dataset-types dataset"));
        }

        return Mono.justOrEmpty(loadModel(datasetTypeRdfSource, false))
                .doOnSuccess(this::updateVersion)
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme, EUDatasetType.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapDatasetType);
    }

    private void updateVersion(Model m) {
        VERSION = m.getProperty(
                m.getResource("http://publications.europa.eu/resource/authority/dataset-type"),
                OWL.versionInfo
        ).getString();
    }

    private DatasetType mapDatasetType(Resource datasetType) {
        return DatasetType.builder()
                .uri(datasetType.getURI())
                .code(datasetType.getProperty(DC.identifier).getObject().toString())
                .label(datasetType.listProperties(SKOS.prefLabel).toList().stream()
                        .map(stmt -> stmt.getObject().asLiteral())
                        .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                        .collect(Collectors.toMap(Literal::getLanguage, Literal::getString)))
                .startUse(datasetType.hasProperty(EUAuthorityOntology.startUse) ?
                        LocalDate.parse(datasetType.getProperty(EUAuthorityOntology.startUse).getString()) : null)
                .build();
    }

    public String sparqlQuery() {
        String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
            "PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
            "PREFIX atres: <http://publications.europa.eu/resource/authority/> " +
            "PREFIX at: <http://publications.europa.eu/ontology/authority/> " +
            "CONSTRUCT { " +
                "atres:dataset-type owl:versionInfo ?version . " +
                "?datasetType skos:inScheme atres:dataset-type . " +
                "?datasetType dc:identifier ?code . " +
                "?datasetType at:start.use ?startUse . " +
                "?datasetType skos:prefLabel ?prefLabel . " +
            "} WHERE { " +
                "atres:dataset-type owl:versionInfo ?version . " +
                "?datasetType skos:inScheme atres:dataset-type . " +
                "?datasetType a skos:Concept . " +
                "?datasetType dc:identifier ?code . " +
                "FILTER(?code != 'OP_DATPRO') . " +
                "OPTIONAL { ?datasetType at:start.use ?startUse . } " +
                "?datasetType skos:prefLabel ?prefLabel . " +
                "FILTER(" +
                    "LANG(?prefLabel) = 'en' || " +
                    "LANG(?prefLabel) = 'no' || " +
                    "LANG(?prefLabel) = 'nb' || " +
                    "LANG(?prefLabel) = 'nn'" +
                ") . " +
            "}";
        return URLEncoder.encode(query, StandardCharsets.UTF_8);
    }
}
