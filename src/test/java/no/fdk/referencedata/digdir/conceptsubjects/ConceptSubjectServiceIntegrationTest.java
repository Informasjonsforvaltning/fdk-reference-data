package no.fdk.referencedata.digdir.conceptsubjects;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.core.HarvestMetrics;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.ApplicationSettings;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class ConceptSubjectServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ConceptSubjectRepository conceptSubjectRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_concept_subjects() {
        ConceptSubjectService conceptSubjectService = new ConceptSubjectService(
                LocalHarvesters.conceptSubject(new ApplicationSettings()),
                conceptSubjectRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        conceptSubjectService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        conceptSubjectRepository.findAll().forEach(conceptSubject -> counter.incrementAndGet());
        assertEquals(4, counter.get());

        final ConceptSubject first = conceptSubjectRepository.findById("https://catalog-admin-service.staging.fellesdatakatalog.digdir.no/910244132/concepts/subjects#555").orElseThrow();
        assertEquals("https://catalog-admin-service.staging.fellesdatakatalog.digdir.no/910244132/concepts/subjects#555", first.getUri());
        assertEquals("555", first.getCode());
        assertEquals("codeName", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        ConceptSubjectRepository conceptSubjectRepositorySpy = spy(this.conceptSubjectRepository);

        ConceptSubject conceptSubject = ConceptSubject.builder()
                .uri("http://uri.no")
                .code("SUBJECT")
                .label(Map.of("en", "My subject"))
                .build();
        conceptSubjectRepositorySpy.save(conceptSubject);

        long count = conceptSubjectRepositorySpy.count();
        assertTrue(count > 0);

        when(conceptSubjectRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new ConceptSubjectService(
                LocalHarvesters.conceptSubject(new ApplicationSettings()),
                conceptSubjectRepositorySpy,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        assertEquals(count, conceptSubjectRepositorySpy.count());
    }
}
