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
        return URLEncoder.encode(
                genericSPARQLQuery("dataset-type"),
                StandardCharsets.UTF_8
        );
    }
}
