package no.fdk.referencedata.geonorge.administrativeenheter;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
public class EnhetHarvester {
    private static final String API_URL = "https://rdf.kartverket.no/api/1.0/adminstrative_unit/search?search=&format=text/turtle";

    public Flux<Enhet> harvest() {
        log.info("Starting harvest of administrative enheter");
        try {
            Property solution = ResourceFactory.createProperty("http://www.w3.org/2005/sparql-results#solution");
            return Mono.justOrEmpty(loadModel(getSource()))
                    .flatMapIterable(m -> m.listObjectsOfProperty(solution).toList())
                    .filter(RDFNode::isResource)
                    .map(RDFNode::asResource)
                    .map(this::mapEnhet);
        } catch(Exception e) {
            log.error("Unable to harvest administrative enheter", e);
            return Flux.error(e);
        }
    }

    public org.springframework.core.io.Resource getSource() {
        try {
            return new UrlResource(API_URL);
        } catch (MalformedURLException e) {
            log.error("Unable to get source", e);
            return null;
        }
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
        Property bindingProperty = ResourceFactory.createProperty("http://www.w3.org/2005/sparql-results#binding");
        Property variableProperty = ResourceFactory.createProperty("http://www.w3.org/2005/sparql-results#variable");
        Property valueProperty = ResourceFactory.createProperty("http://www.w3.org/2005/sparql-results#value");

        enhet.listProperties(bindingProperty).forEach(binding -> {
            String variableType = binding.getObject().asResource().getProperty(variableProperty).getObject().asLiteral().getString();

            if (Objects.equals(variableType, "uri")) {
                String uri = binding.getObject().asResource().getProperty(valueProperty).getObject().asResource().getURI();
                builder.uri(uri);
                String[] splitURI = uri.split("/");
                builder.code(splitURI[splitURI.length - 1]);
            } else if (Objects.equals(variableType, "enh_navn")) {
                String name = binding.getObject().asResource().getProperty(valueProperty).getObject().asLiteral().getString();
                builder.name(name);
            }
        });

        return builder.build();
    }

}
