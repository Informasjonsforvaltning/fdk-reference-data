package no.fdk.referencedata.los;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.fdk.referencedata.core.HarvestMetrics;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.HarvestSourceException;
import no.fdk.referencedata.core.HarvestTrigger;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;
import no.fdk.referencedata.core.ReferenceDataWriter;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.UrlResource;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class LosImporterTest {

    @Mock
    private ReferenceDataWriter referenceDataWriter;
    @Mock
    private RDFSourceRepository rdfSourceRepository;
    @Mock
    private LosRepository losRepository;

    @Test
    void importFromUnavailableSourceThrowsHarvestSourceException() throws MalformedURLException {
        LosImporter importer = new LosImporter() {
            @Override
            public org.springframework.core.io.Resource getSource() {
                try {
                    return new UrlResource("https://example.invalid/missing.rdf");
                } catch (MalformedURLException e) {
                    throw new HarvestSourceException("Unable to get LOS source", e);
                }
            }
        };

        HarvestSourceException exception = assertThrows(
                HarvestSourceException.class,
                importer::importFromLosSource);

        assertTrue(exception.getMessage().contains("Unable to load LOS model"));
        assertEquals("source", exception.reasonTag());
    }

    @Test
    void sourceFailureSurfacesAsSourceReasonThroughLosService() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        HarvestMetrics harvestMetrics = new HarvestMetrics(meterRegistry);
        LosService losService = new LosService(
                new LosImporter() {
                    @Override
                    public org.springframework.core.io.Resource getSource() {
                        try {
                            return new UrlResource("https://example.invalid/missing.rdf");
                        } catch (MalformedURLException e) {
                            throw new HarvestSourceException("Unable to get LOS source", e);
                        }
                    }
                },
                losRepository,
                new ReferenceDataServiceSupport(referenceDataWriter, rdfSourceRepository, harvestMetrics),
                harvestMetrics);

        HarvestResult result = losService.importLosNodes();

        assertEquals(HarvestResult.Outcome.FAILURE, result.outcome());
        assertEquals("source", result.reason());
        assertEquals(1.0, meterRegistry.get("reference_data_harvest_total")
                .tag("module", "los")
                .tag("outcome", "failure")
                .tag("reason", "source")
                .tag("trigger", HarvestTrigger.UNKNOWN)
                .counter()
                .count());
    }
}
