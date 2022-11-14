package no.fdk.referencedata.eu;

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
public abstract class AbstractEuHarvester<T> {
    private static final String DOWNLOAD_API = "https://op.europa.eu/o/opportal-service/euvoc-download-handler";

    public abstract String getVersion();

    public Resource getSource(final String cellarURI, final String fileName) {
        try {
            return new UrlResource(DOWNLOAD_API + "?cellarURI=" + cellarURI + "&fileName=" + fileName);
        } catch (MalformedURLException e) {
            log.error("Unable to get source", e);
            return null;
        }
    }

    protected Optional<Model> getModel(Resource resource) {
        try {
            return Optional.of(RDFDataMgr.loadModel(resource.getURI().toString(), Lang.RDFXML));
        } catch (IOException e) {
            log.error("Unable to load model", e);
            return Optional.empty();
        }
    }

    public abstract Flux<T> harvest();
}
