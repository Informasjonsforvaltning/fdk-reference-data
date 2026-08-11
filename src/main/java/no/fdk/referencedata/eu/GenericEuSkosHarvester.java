package no.fdk.referencedata.eu;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.core.HarvestSourceException;
import no.fdk.referencedata.eu.vocabulary.EUAuthorityOntology;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.SKOS;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Slf4j
public abstract class GenericEuSkosHarvester<T> extends AbstractEuHarvester<T> {

    protected abstract String schemaName();

    protected abstract Resource scheme();

    protected abstract String logName();

    protected abstract T mapConcept(Resource concept);

    @Override
    public Flux<T> harvest() {
        log.info("Starting harvest of EU {}", logName());
        final org.springframework.core.io.Resource rdfSource = getSource();

        return Mono.justOrEmpty(loadModel(rdfSource, false))
                .switchIfEmpty(Mono.error(new HarvestSourceException("Unable to fetch " + logName() + " distribution")))
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme, scheme()).toList())
                .filter(Resource::isURIResource)
                .map(this::mapConcept);
    }

    @Override
    public String sparqlQuery() {
        return URLEncoder.encode(
                genericSPARQLQuery(schemaName()),
                StandardCharsets.UTF_8
        );
    }

    protected static String extractCode(Resource resource) {
        return resource.getProperty(DC.identifier).getObject().toString();
    }

    protected static LocalDate extractStartUse(Resource resource) {
        return resource.hasProperty(EUAuthorityOntology.startUse)
                ? LocalDate.parse(resource.getProperty(EUAuthorityOntology.startUse).getString())
                : null;
    }
}
