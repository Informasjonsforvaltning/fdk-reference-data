package no.fdk.referencedata.eu;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.datatheme.DataTheme;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public abstract class AbstractEuHarvester<T> {

    private final String dataset;

    private final String distributionFile;

    private final EuDatasetFetcher euDatasetFetcher;

    public AbstractEuHarvester(final String dataset, final String distributionFile) {
        this.dataset = dataset;
        this.distributionFile = distributionFile;
        this.euDatasetFetcher = new EuDatasetFetcher(dataset);
    }

    public String getVersion() {
        try {
            return euDatasetFetcher.getVersion();
        } catch(Exception e) {
            log.error("Unable to fetch latest " + dataset + " version", e);
            return "0";
        }
    }

    public org.springframework.core.io.Resource getSource() {
        try {
            return euDatasetFetcher.fetchResource(this.distributionFile);
        } catch(Exception e) {
            log.error("Unable to retrieve " + dataset + " source", e);
        }
        return null;
    }

    protected Optional<Model> getModel(org.springframework.core.io.Resource resource) {
        try {
            return Optional.of(RDFDataMgr.loadModel(resource.getURI().toString(), Lang.RDFXML));
        } catch (IOException e) {
            log.error("Unable to load model", e);
            return Optional.empty();
        }
    }

    public abstract Flux<T> harvest();
}
