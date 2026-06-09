package no.fdk.referencedata.eu.datasettype;

import no.fdk.referencedata.eu.datasettype.DatasetTypeWriter;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.eu.datasettype.LocalDatasetTypeHarvester.DATASET_TYPES_SIZE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class DatasetTypeServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private DatasetTypeRepository datasetTypeRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_dataset_types() {
        DatasetTypeService accessRightService = new DatasetTypeService(
                new LocalDatasetTypeHarvester(),
                datasetTypeRepository,
                rdfSourceRepository,
                new DatasetTypeWriter(datasetTypeRepository, rdfSourceRepository));

        accessRightService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        datasetTypeRepository.findAll().forEach(accessRight -> counter.incrementAndGet());
        assertEquals(DATASET_TYPES_SIZE, counter.get());

        final DatasetType first = datasetTypeRepository.findById("http://publications.europa.eu/resource/authority/dataset-type/NAL").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/dataset-type/NAL", first.getUri());
        assertEquals("NAL", first.getCode());
        assertEquals("Name authority list", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        DatasetTypeRepository datasetTypeRepositorySpy = spy(this.datasetTypeRepository);

        DatasetType datasetType = DatasetType.builder()
                .uri("http://uri.no")
                .code("DATASET_TYPE_A")
                .label(Map.of("en", "My dataset type"))
                .build();
        datasetTypeRepositorySpy.save(datasetType);

        long count = datasetTypeRepositorySpy.count();
        assertTrue(count > 0);

        when(datasetTypeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new DatasetTypeService(
                new LocalDatasetTypeHarvester(),
                datasetTypeRepository,
                rdfSourceRepository,
                new DatasetTypeWriter(datasetTypeRepository, rdfSourceRepository));

        assertEquals(count, datasetTypeRepositorySpy.count());
    }
}
