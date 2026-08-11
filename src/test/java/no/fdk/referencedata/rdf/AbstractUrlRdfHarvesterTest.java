package no.fdk.referencedata.rdf;

import no.fdk.referencedata.core.HarvestSourceException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.UrlResource;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractUrlRdfHarvesterTest {

    @Test
    void loadModelThrowsHarvestSourceExceptionWhenResourceUnavailable() throws MalformedURLException {
        AbstractUrlRdfHarvester<String> harvester = new AbstractUrlRdfHarvester<>() {
            @Override
            protected String baseUri() {
                return "https://example.invalid/";
            }

            @Override
            public Flux<String> harvest() {
                return Flux.empty();
            }
        };

        UrlResource missing = new UrlResource("https://example.invalid/missing.ttl");

        HarvestSourceException exception = assertThrows(
                HarvestSourceException.class,
                () -> harvester.loadModel(missing));

        assertTrue(exception.getMessage().contains("Unable to load model"));
        assertEquals("source", exception.reasonTag());
    }
}
