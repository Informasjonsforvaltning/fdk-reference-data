package no.fdk.referencedata.digdir;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;

@Component
@Slf4j
public abstract class AbstractDataNorgeHarvester<T> {
    private static final String BASE_URI = "https://data.norge.no/vocabulary/";

    public abstract String getVersion();

    public Resource getSource(final String path) {
        try {
            return new UrlResource(BASE_URI + path);
        } catch (MalformedURLException e) {
            log.error("Unable to get source", e);
            return null;
        }
    }

    protected Optional<Model> getModel(Resource resource) {
        try {
            return Optional.of(RDFDataMgr.loadModel(resource.getURI().toString(), Lang.TURTLE));
        } catch (IOException e) {
            log.error("Unable to load model", e);
            return Optional.empty();
        }
    }

    public abstract Flux<T> harvest();
}
