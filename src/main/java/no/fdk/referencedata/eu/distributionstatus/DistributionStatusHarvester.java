package no.fdk.referencedata.eu.distributionstatus;

import no.fdk.referencedata.rdf.SkosMapper;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.eu.vocabulary.EUAuthorityOntology;
import no.fdk.referencedata.eu.vocabulary.EUDistributionStatus;
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
public class DistributionStatusHarvester extends AbstractEuHarvester<DistributionStatus> {


    public DistributionStatusHarvester() {
        super();
    }


    public Flux<DistributionStatus> harvest() {
        log.info("Starting harvest of EU distribution statuses");
        final org.springframework.core.io.Resource dataThemesRdfSource = getSource();
        if(dataThemesRdfSource == null) {
            return Flux.error(new Exception("Unable to fetch distribution statuses"));
        }

        return Mono.justOrEmpty(loadModel(dataThemesRdfSource, false))
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme, EUDistributionStatus.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapDistributionStatus);
    }


    private DistributionStatus mapDistributionStatus(Resource distributionStatus) {
        return DistributionStatus.builder()
                .uri(distributionStatus.getURI())
                .code(distributionStatus.getProperty(DC.identifier).getObject().toString())
                .label(SkosMapper.extractLabels(distributionStatus))
                .startUse(distributionStatus.hasProperty(EUAuthorityOntology.startUse) ?
                        LocalDate.parse(distributionStatus.getProperty(EUAuthorityOntology.startUse).getString()) : null)
                .build();
    }

    public String sparqlQuery() {
        return URLEncoder.encode(
                genericSPARQLQuery("distribution-status"),
                StandardCharsets.UTF_8
        );
    }
}
