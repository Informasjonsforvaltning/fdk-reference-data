package no.fdk.referencedata.core;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferenceDataServiceSupportTest {

    @Mock
    private ReferenceDataWriter referenceDataWriter;
    @Mock
    private RDFSourceRepository rdfSourceRepository;
    @Mock
    private JpaRepository<String, String> repository;
    @Mock
    private ModelHarvester<String> harvester;

    private SimpleMeterRegistry meterRegistry;
    private ReferenceDataServiceSupport support;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        support = new ReferenceDataServiceSupport(
                referenceDataWriter,
                rdfSourceRepository,
                new HarvestMetrics(meterRegistry));
    }

    @Test
    void harvestAndSaveSuccessPersistsAndRecordsMetrics() {
        when(harvester.harvest()).thenReturn(Flux.just("a", "b"));
        when(harvester.getModel()).thenReturn(ModelFactory.createDefaultModel());

        HarvestResult result = support.harvestAndSave(harvester, repository, "source-id", "access-right");

        assertEquals(HarvestResult.Outcome.SUCCESS, result.outcome());
        assertEquals(2, result.itemCount());
        verify(referenceDataWriter).replaceAll(eq(repository), eq(List.of("a", "b")), any(RDFSource.class));
        assertEquals(1.0, meterRegistry.get(HarvestMetrics.METRIC_TOTAL)
                .tag("module", "access-right")
                .tag("outcome", "success")
                .tag("reason", "none")
                .counter()
                .count());
        assertEquals(2.0, meterRegistry.get(HarvestMetrics.METRIC_ITEMS)
                .tag("module", "access-right")
                .gauge()
                .value());
    }

    @Test
    void harvestAndSaveEmptySkipsReplaceAndRecordsSkippedEmpty() {
        when(harvester.harvest()).thenReturn(Flux.empty());

        HarvestResult result = support.harvestAndSave(harvester, repository, "source-id", "access-right");

        assertEquals(HarvestResult.Outcome.SKIPPED_EMPTY, result.outcome());
        verify(referenceDataWriter, never()).replaceAll(any(), anyList(), any());
        verify(referenceDataWriter, never()).replaceAll(any(), anyList());
        assertEquals(1.0, meterRegistry.get(HarvestMetrics.METRIC_TOTAL)
                .tag("module", "access-right")
                .tag("outcome", "skipped_empty")
                .tag("reason", "empty")
                .counter()
                .count());
    }

    @Test
    void harvestAndSaveExceptionRecordsFailureWithoutPersisting() {
        when(harvester.harvest()).thenReturn(Flux.error(new RuntimeException("harvest failed")));

        HarvestResult result = support.harvestAndSave(harvester, repository, "source-id", "access-right");

        assertEquals(HarvestResult.Outcome.FAILURE, result.outcome());
        verify(referenceDataWriter, never()).replaceAll(any(), anyList(), any());
        assertEquals(1.0, meterRegistry.get(HarvestMetrics.METRIC_TOTAL)
                .tag("module", "access-right")
                .tag("outcome", "failure")
                .tag("reason", "error")
                .counter()
                .count());
    }

    @Test
    void harvestAndSaveWithoutRdfSuccessPersists() {
        HarvestResult result = support.harvestAndSaveWithoutRdf(
                () -> Flux.just("x"),
                repository,
                "fylke-organisasjon");

        assertEquals(HarvestResult.Outcome.SUCCESS, result.outcome());
        assertEquals(1, result.itemCount());
        verify(referenceDataWriter).replaceAll(repository, List.of("x"));
    }

    @Test
    void persistHarvestedEmptySkipsReplace() {
        HarvestResult result = support.persistHarvested(
                "media-type",
                List.of(),
                ModelFactory.createDefaultModel(),
                repository,
                "source-id");

        assertEquals(HarvestResult.Outcome.SKIPPED_EMPTY, result.outcome());
        verify(referenceDataWriter, never()).replaceAll(any(), anyList(), any());
    }
}
