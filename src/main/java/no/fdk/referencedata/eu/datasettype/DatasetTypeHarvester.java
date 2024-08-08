package no.fdk.referencedata.eu.datasettype;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.eu.vocabulary.EUAuthorityOntology;
import no.fdk.referencedata.eu.vocabulary.EUDatasetType;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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
import java.util.Map;
import java.util.Optional;
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

    private final Map<String, Map<String, String>> overrideTranslations = Map.ofEntries(
            Map.entry(EUDatasetType.getURI() + "/HVD", Map.ofEntries(
                    Map.entry("nb", "Datasett med høy verdi"),
                    Map.entry("nn", "Datasett med høg verdi"),
                    Map.entry("no", "Datasett med høy verdi")
            )),
            Map.entry(EUDatasetType.getURI() + "/RELEASE", Map.ofEntries(
                    Map.entry("nb", "Versjon"),
                    Map.entry("nn", "Versjon"),
                    Map.entry("no", "Versjon")
            )),
            Map.entry(EUDatasetType.getURI() + "/STATISTICAL", Map.ofEntries(
                    Map.entry("nb", "Statistiske data"),
                    Map.entry("nn", "Statistiske data"),
                    Map.entry("no", "Statistiske data")
            )),
            Map.entry(EUDatasetType.getURI() + "/SYNTHETIC_DATA", Map.ofEntries(
                    Map.entry("nb", "Syntetiske data"),
                    Map.entry("nn", "Syntetiske data"),
                    Map.entry("no", "Syntetiske data")
            ))
    );

    private final Map<String, Map<String, String>> missingTranslations = Map.ofEntries(
            Map.entry(EUDatasetType.getURI() + "/HVD", Map.ofEntries(
                    Map.entry("nb", "Datasett med høy verdi"),
                    Map.entry("nn", "Datasett med høg verdi"),
                    Map.entry("no", "Datasett med høy verdi")
            )),
            Map.entry(EUDatasetType.getURI() + "/RELEASE", Map.ofEntries(
                    Map.entry("nb", "Versjon"),
                    Map.entry("nn", "Versjon"),
                    Map.entry("no", "Versjon")
            ))
    );

    public Model translateDatasetTypes(Model model) {
        Model translated = ModelFactory.createDefaultModel();

        model.listStatements().forEach(stmt -> {
            if (stmt.getSubject().isURIResource() && overrideTranslations.containsKey(stmt.getSubject().getURI())) {
                Map<String, String> subjectTranslations = overrideTranslations.get(stmt.getSubject().getURI());
                if (stmt.getPredicate().hasURI(SKOS.prefLabel.getURI()) && subjectTranslations.containsKey(stmt.getLanguage())) {
                    translated.add(
                            stmt.getSubject(),
                            stmt.getPredicate(),
                            subjectTranslations.get(stmt.getLanguage()),
                            stmt.getLanguage()
                    );
                } else {
                    translated.add(stmt);
                }
            } else {
                translated.add(stmt);
            }
        });

        for (String subject : missingTranslations.keySet()) {
            Resource subjectResource = model.getResource(subject);
            Map<String, String> subjectTranslations = overrideTranslations.get(subject);
            for (String language : subjectTranslations.keySet()) {
                translated.add(
                        subjectResource,
                        SKOS.prefLabel,
                        subjectTranslations.get(language),
                        language
                );
            }
        }

        updateModel(translated);
        return translated;
    }

    private Optional<Model> loadAndTranslateModel(org.springframework.core.io.Resource datasetTypeRdfSource) {
        return loadModel(datasetTypeRdfSource, false)
                .map(this::translateDatasetTypes);
    }

    public Flux<DatasetType> harvest() {
        log.info("Starting harvest of EU dataset types");
        final org.springframework.core.io.Resource datasetTypeRdfSource = getSource();
        if(datasetTypeRdfSource == null) {
            return Flux.error(new Exception("Unable to fetch dataset-types dataset"));
        }

        return Mono.justOrEmpty(loadAndTranslateModel(datasetTypeRdfSource))
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
