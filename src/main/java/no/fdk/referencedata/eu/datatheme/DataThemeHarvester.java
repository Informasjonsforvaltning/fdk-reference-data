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
    private static final String cellarURI = "http://publications.europa.eu/resource/cellar/34802596-0286-11ed-acce-01aa75ed71a1.0001.01/DOC_1";
    private static final String rdfFileName = "data-theme-skos-ap-act.rdf";
    private static String VERSION = "0";

    public DataThemeHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<DataTheme> harvest() {
        log.info("Starting harvest of EU data themes");
        final org.springframework.core.io.Resource dataThemesRdfSource = getSource(cellarURI, rdfFileName);
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
                    .versionNumber(resource.getProperty(EUAuthorityOntology.tableVersionNumber).getString())
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
}
