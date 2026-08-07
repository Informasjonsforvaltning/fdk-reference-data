package no.fdk.referencedata.digdir;

import no.fdk.referencedata.rdf.AbstractUrlRdfHarvester;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractDataNorgeHarvester<T> extends AbstractUrlRdfHarvester<T> {

    private static final String BASE_URI = "https://data.norge.no/vocabulary/";

    @Override
    protected String baseUri() {
        return BASE_URI;
    }
}
