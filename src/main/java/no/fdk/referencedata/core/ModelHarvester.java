package no.fdk.referencedata.core;

import org.apache.jena.rdf.model.Model;
import reactor.core.publisher.Flux;

public interface ModelHarvester<T> {
    Flux<T> harvest();

    Model getModel();
}
