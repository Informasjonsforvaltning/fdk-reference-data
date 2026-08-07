package no.fdk.referencedata.eu.highvaluecategories;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.eu.highvaluecategories.LocalHighValueCategoryHarvester.HIGH_VALUE_CATEGORIES_SIZE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class HighValueCategoryServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private HighValueCategoryRepository highValueCategoryRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_high_value_categories() {
        HighValueCategoryService highValueCategoryService = new HighValueCategoryService(
                new LocalHighValueCategoryHarvester(),
                highValueCategoryRepository,
                rdfSourceRepository,
                new ReferenceDataWriter(rdfSourceRepository));

        highValueCategoryService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        highValueCategoryRepository.findAll().forEach(category -> counter.incrementAndGet());
        assertEquals(HIGH_VALUE_CATEGORIES_SIZE, counter.get());

        final HighValueCategory first = highValueCategoryRepository.findById("http://data.europa.eu/bna/c_164e0bf5").orElseThrow();
        assertEquals("http://data.europa.eu/bna/c_164e0bf5", first.getUri());
        assertEquals("c_164e0bf5", first.getCode());
        assertEquals("Meteorological", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        HighValueCategoryRepository highValueCategoryRepositorySpy = spy(this.highValueCategoryRepository);

        HighValueCategory category = HighValueCategory.builder()
                .uri("http://uri.no")
                .code("TEST_CATEGORY")
                .label(Map.of("en", "My category"))
                .build();
        highValueCategoryRepositorySpy.save(category);

        long count = highValueCategoryRepositorySpy.count();
        assertTrue(count > 0);

        when(highValueCategoryRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new HighValueCategoryService(
                new LocalHighValueCategoryHarvester(),
                highValueCategoryRepositorySpy,
                rdfSourceRepository,
                new ReferenceDataWriter(rdfSourceRepository));

        assertEquals(count, highValueCategoryRepositorySpy.count());
    }
}
