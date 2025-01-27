package no.fdk.referencedata.eu.plannedavailability;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.eu.vocabulary.EUAuthorityOntology;
import no.fdk.referencedata.eu.vocabulary.EUPlannedAvailability;
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
public class PlannedAvailabilityHarvester extends AbstractEuHarvester<PlannedAvailability> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());
    private static String VERSION = "0";

    public PlannedAvailabilityHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<PlannedAvailability> harvest() {
        log.info("Starting harvest of EU planned availability");
        final org.springframework.core.io.Resource rdfSource = getSource();
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch planned availability"));
        }

        return Mono.justOrEmpty(loadModel(rdfSource, false))
                .doOnSuccess(this::updateVersion)
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme, EUPlannedAvailability.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapPlannedAvailability);
    }

    private void updateVersion(Model m) {
        VERSION = m.getProperty(
                m.getResource("http://publications.europa.eu/resource/authority/planned-availability"),
                OWL.versionInfo
        ).getString();
    }

    private PlannedAvailability mapPlannedAvailability(Resource plannedAvailability) {
        return PlannedAvailability.builder()
                .uri(plannedAvailability.getURI())
                .code(plannedAvailability.getProperty(DC.identifier).getObject().toString())
                .label(plannedAvailability.listProperties(SKOS.prefLabel).toList().stream()
                        .map(stmt -> stmt.getObject().asLiteral())
                        .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                        .collect(Collectors.toMap(Literal::getLanguage, Literal::getString)))
                .startUse(plannedAvailability.hasProperty(EUAuthorityOntology.startUse) ?
                        LocalDate.parse(plannedAvailability.getProperty(EUAuthorityOntology.startUse).getString()) : null)
                .build();
    }

    public String sparqlQuery() {
        return URLEncoder.encode(
                genericSPARQLQuery("planned-availability"),
                StandardCharsets.UTF_8
        );
    }
}
