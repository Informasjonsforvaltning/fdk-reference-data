package no.fdk.referencedata.geonorge.administrativeenheter;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Optional;

@Component
@Slf4j
public class EnhetHarvester {
    private static final String FILE_PATH = "rdf/administrative-enheter.ttl";

    public Flux<Enhet> harvest() {
        log.info("Starting harvest of administrative enheter");
        try {
            return Mono.justOrEmpty(loadModel(getSource()))
                .flatMapIterable(m -> m.listResourcesWithProperty(RDF.type, DCTerms.Location).toList())
                .map(this::mapEnhet);
        } catch (Exception e) {
            log.error("Unable to harvest administrative enheter", e);
            return Flux.error(e);
        }
    }

    public org.springframework.core.io.Resource getSource() {
        return new ClassPathResource(FILE_PATH);
    }

    protected Optional<Model> loadModel(org.springframework.core.io.Resource resource) {
        try {
            return Optional.of(
                RDFDataMgr.loadModel(
                    resource.getURI().toString(),
                    Lang.TURTLE
                )
            );
        } catch (IOException e) {
            log.error("Unable to load model", e);
            return Optional.empty();
        }
    }

    private Enhet mapEnhet(Resource enhet) {
        Enhet.EnhetBuilder builder = Enhet.builder();
        builder.uri(enhet.getURI());
        builder.code(enhet.getProperty(DCTerms.identifier).getObject().asLiteral().getString());
        builder.name(enhet.getProperty(DCTerms.title).getObject().asLiteral().getString());

        return builder.build();
    }
}
