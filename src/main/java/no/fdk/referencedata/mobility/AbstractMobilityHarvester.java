package no.fdk.referencedata.mobility;

import no.fdk.referencedata.rdf.AbstractUrlRdfHarvester;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractMobilityHarvester<T> extends AbstractUrlRdfHarvester<T> {

    private static final String BASE_URI = "https://mobilitydcat-ap.github.io/controlled-vocabularies/";

    @Override
    protected String baseUri() {
        return BASE_URI;
    }
}
