package no.fdk.referencedata.geonorge.administrativeenheter;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.core.HarvestParseException;
import no.fdk.referencedata.core.HarvestSourceException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Component
@Slf4j
public class EnhetHarvester {
    private static final String FILE_PATH = "rdf/administrative-enheter.ttl";

    public Flux<Enhet> harvest() {
        log.info("Starting harvest of administrative enheter");
        try {
            return Mono.just(loadModel(getSource()))
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

    protected Model loadModel(org.springframework.core.io.Resource resource) {
        try {
            return RDFDataMgr.loadModel(resource.getURI().toString(), Lang.TURTLE);
        } catch (IOException e) {
            throw new HarvestSourceException("Unable to load administrative enheter model", e);
        } catch (RiotException e) {
            throw new HarvestParseException("Unable to parse administrative enheter model", e);
        } catch (RuntimeException e) {
            throw new HarvestSourceException("Unable to load administrative enheter model", e);
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
