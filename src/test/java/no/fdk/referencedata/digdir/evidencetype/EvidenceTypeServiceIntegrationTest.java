package no.fdk.referencedata.digdir.evidencetype;

import no.fdk.referencedata.core.ReferenceDataWriter;

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
public class EvidenceTypeServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private EvidenceTypeRepository evidenceTypeRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_evidence_types() {
        EvidenceTypeService evidenceTypeService = new EvidenceTypeService(
                new LocalEvidenceTypeHarvester(),
                evidenceTypeRepository,
                rdfSourceRepository,
                new ReferenceDataWriter(rdfSourceRepository));

        evidenceTypeService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        evidenceTypeRepository.findAll().forEach(evidenceType -> counter.incrementAndGet());
        assertEquals(4, counter.get());

        final EvidenceType first = evidenceTypeRepository.findById("https://data.norge.no/vocabulary/evidence-type#declaration").orElseThrow();
        assertEquals("https://data.norge.no/vocabulary/evidence-type#declaration", first.getUri());
        assertEquals("declaration", first.getCode());
        assertEquals("declaration", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        EvidenceTypeRepository evidenceTypeRepositorySpy = spy(this.evidenceTypeRepository);

        EvidenceType evidenceType = EvidenceType.builder()
                .uri("http://uri.no")
                .code("EVIDENCE_TYPE")
                .label(Map.of("en", "My evidence"))
                .build();
        evidenceTypeRepositorySpy.save(evidenceType);

        long count = evidenceTypeRepositorySpy.count();
        assertTrue(count > 0);

        when(evidenceTypeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new EvidenceTypeService(
                new LocalEvidenceTypeHarvester(),
                evidenceTypeRepositorySpy,
                rdfSourceRepository,
                new ReferenceDataWriter(rdfSourceRepository));

        assertEquals(count, evidenceTypeRepositorySpy.count());
    }
}
