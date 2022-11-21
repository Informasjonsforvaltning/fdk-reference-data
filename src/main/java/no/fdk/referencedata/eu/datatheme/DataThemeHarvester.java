package no.fdk.referencedata.eu.datatheme;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.eu.vocabulary.EUAuthorityOntology;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.eu.vocabulary.EUDataTheme;
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
public class DataThemeHarvester extends AbstractEuHarvester<DataTheme> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());
    private static String VERSION = "0";

    public DataThemeHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<DataTheme> harvest() {
        log.info("Starting harvest of EU data themes");
        final org.springframework.core.io.Resource dataThemesRdfSource = getSource(sparqlQuery());
        if(dataThemesRdfSource == null) {
            return Flux.error(new Exception("Unable to fetch data-theme distribution"));
        }

        return Mono.justOrEmpty(getModel(dataThemesRdfSource))
                .doOnSuccess(this::updateVersion)
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme, EUDataTheme.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapDataTheme);
    }

    private void updateVersion(Model m) {
        VERSION = m.getProperty(
                m.getResource("http://publications.europa.eu/resource/authority/data-theme"),
                OWL.versionInfo
        ).getString();
    }

    private DataTheme mapDataTheme(Resource dataTheme) {
        final ConceptSchema conceptSchema = Mono.justOrEmpty(dataTheme.getProperty(SKOS.inScheme).getResource())
                .map(resource -> ConceptSchema.builder()
                    .uri(resource.getURI())
                    .label(resource.listProperties(SKOS.prefLabel).toList().stream()
                            .map(stmt -> stmt.getObject().asLiteral())
                            .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                            .collect(Collectors.toMap(Literal::getLanguage, Literal::getString)))
                    .versionNumber(resource.getProperty(OWL.versionInfo).getString())
                    .build())
                .block();

        return DataTheme.builder()
                .uri(dataTheme.getURI())
                .code(dataTheme.getProperty(DC.identifier).getObject().toString())
                .label(dataTheme.listProperties(SKOS.prefLabel).toList().stream()
                        .map(stmt -> stmt.getObject().asLiteral())
                        .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                        .collect(Collectors.toMap(Literal::getLanguage, Literal::getString)))
                .startUse(dataTheme.hasProperty(EUAuthorityOntology.startUse) ?
                        LocalDate.parse(dataTheme.getProperty(EUAuthorityOntology.startUse).getString()) : null)
                .conceptSchema(conceptSchema)
                .build();
    }

    private String sparqlQuery() {
        String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
            "PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
            "PREFIX atres: <http://publications.europa.eu/resource/authority/> " +
            "PREFIX at: <http://publications.europa.eu/ontology/authority/> " +
            "CONSTRUCT { " +
                "atres:data-theme owl:versionInfo ?version . " +
                "atres:data-theme skos:prefLabel ?schemaLabel . " +
                "?dataTheme skos:inScheme ?inScheme . " +
                "?dataTheme dc:identifier ?code . " +
                "?dataTheme at:start.use ?startUse . " +
                "?dataTheme skos:prefLabel ?prefLabel . " +
            "} WHERE { " +
                "atres:data-theme owl:versionInfo ?version . " +
                "atres:data-theme skos:prefLabel ?schemaLabel . " +
                "FILTER(" +
                    "LANG(?schemaLabel) = 'en' || " +
                    "LANG(?schemaLabel) = 'no' || " +
                    "LANG(?schemaLabel) = 'nb' || " +
                    "LANG(?schemaLabel) = 'nn'" +
                ") . " +
                "?dataTheme skos:inScheme atres:data-theme . " +
                "?dataTheme skos:inScheme ?inScheme . " +
                "?dataTheme a skos:Concept . " +
                "?dataTheme dc:identifier ?code . " +
                "OPTIONAL { ?dataTheme at:start.use ?startUse . } " +
                "?dataTheme skos:prefLabel ?prefLabel . " +
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
