package no.fdk.referencedata.digdir.legalresourcetype;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.digdir.legalresourcetype.LocalLegalResourceTypeHarvester.LEGAL_RESOURCE_TYPES_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class LegalResourceTypeServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private LegalResourceTypeRepository legalResourceTypeRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @BeforeEach
    public void setup() {
        legalResourceTypeRepository.deleteAll();
    }

    @Test
    public void test_if_harvest_persists_legal_resource_types() {
        LegalResourceTypeService legalResourceTypeService = new LegalResourceTypeService(
                new LocalLegalResourceTypeHarvester(),
                legalResourceTypeRepository,
                rdfSourceRepository,
                new ReferenceDataWriter(rdfSourceRepository));

        legalResourceTypeService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        legalResourceTypeRepository.findAll().forEach(legalResourceType -> counter.incrementAndGet());
        assertEquals(LEGAL_RESOURCE_TYPES_SIZE, counter.get());

        final LegalResourceType first = legalResourceTypeRepository.findById("https://data.norge.no/vocabulary/legal-resource-type#act").orElseThrow();
        assertEquals("https://data.norge.no/vocabulary/legal-resource-type#act", first.getUri());
        assertEquals("act", first.getCode());
        assertEquals("act", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        LegalResourceTypeRepository legalResourceTypeRepositorySpy = spy(this.legalResourceTypeRepository);

        LegalResourceType legalResourceType = LegalResourceType.builder()
                .uri("http://uri.no")
                .code("LEGAL_RESOURCE_TYPE")
                .label(Map.of("en", "My type"))
                .build();
        legalResourceTypeRepositorySpy.save(legalResourceType);

        long count = legalResourceTypeRepositorySpy.count();
        assertTrue(count > 0);

        when(legalResourceTypeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new LegalResourceTypeService(
                new LocalLegalResourceTypeHarvester(),
                legalResourceTypeRepositorySpy,
                rdfSourceRepository,
                new ReferenceDataWriter(rdfSourceRepository));

        assertEquals(count, legalResourceTypeRepositorySpy.count());
    }
}
