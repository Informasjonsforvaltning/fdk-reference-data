package no.fdk.referencedata.eu.plannedavailability;

import no.fdk.referencedata.rdf.SkosMapper;

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


    public PlannedAvailabilityHarvester() {
        super();
    }


    public Flux<PlannedAvailability> harvest() {
        log.info("Starting harvest of EU planned availability");
        final org.springframework.core.io.Resource rdfSource = getSource();
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch planned availability"));
        }

        return Mono.justOrEmpty(loadModel(rdfSource, false))
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme, EUPlannedAvailability.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapPlannedAvailability);
    }


    private PlannedAvailability mapPlannedAvailability(Resource plannedAvailability) {
        return PlannedAvailability.builder()
                .uri(plannedAvailability.getURI())
                .code(plannedAvailability.getProperty(DC.identifier).getObject().toString())
                .label(SkosMapper.extractLabels(plannedAvailability))
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
