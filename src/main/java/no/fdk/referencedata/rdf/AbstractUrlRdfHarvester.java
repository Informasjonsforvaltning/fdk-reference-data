package no.fdk.referencedata.rdf;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.core.HarvestParseException;
import no.fdk.referencedata.core.HarvestSourceException;
import no.fdk.referencedata.core.ModelHarvester;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.MalformedURLException;

@Slf4j
public abstract class AbstractUrlRdfHarvester<T> implements ModelHarvester<T> {

    @Getter
    private final Model model = ModelFactory.createDefaultModel();

    protected abstract String baseUri();

    public Resource getSource(final String path) {
        try {
            return new UrlResource(baseUri() + path);
        } catch (MalformedURLException e) {
            throw new HarvestSourceException("Unable to get source for " + path, e);
        }
    }

    protected void loadModel(Resource resource) {
        Model fetched = fetchModel(resource);
        model.removeAll();
        model.add(fetched);
    }

    private Model fetchModel(Resource resource) {
        try {
            return RDFDataMgr.loadModel(resource.getURI().toString(), Lang.TURTLE);
        } catch (IOException e) {
            throw new HarvestSourceException("Unable to load model", e);
        } catch (RiotException e) {
            throw new HarvestParseException("Unable to parse model", e);
        } catch (RuntimeException e) {
            throw new HarvestSourceException("Unable to load model", e);
        }
    }

    public abstract Flux<T> harvest();
}
