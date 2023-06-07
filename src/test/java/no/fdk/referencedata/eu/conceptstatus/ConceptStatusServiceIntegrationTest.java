package no.fdk.referencedata.eu.conceptstatus;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.eu.conceptstatus.LocalConceptStatusHarvester.CONCEPT_STATUSES_SIZE;
import static no.fdk.referencedata.settings.Settings.CONCEPT_STATUS;
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
public class ConceptStatusServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ConceptStatusRepository conceptStatusRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @BeforeEach
    public void setup() {
        ConceptStatusService conceptStatusService = new ConceptStatusService(
                new LocalConceptStatusHarvester("1"),
                conceptStatusRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        conceptStatusService.harvestAndSave(true);
    }

    @Test
    public void test_if_harvest_persists_concept_statuses() {
        ConceptStatusService conceptStatusService = new ConceptStatusService(
                new LocalConceptStatusHarvester("20200923-0"),
                conceptStatusRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        conceptStatusService.harvestAndSave(false);

        final AtomicInteger counter = new AtomicInteger();
        conceptStatusRepository.findAll().forEach(conceptStatus -> counter.incrementAndGet());
        assertEquals(CONCEPT_STATUSES_SIZE, counter.get());

        final ConceptStatus first = conceptStatusRepository.findById("http://publications.europa.eu/resource/authority/concept-status/CANDIDATE").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/concept-status/CANDIDATE", first.getUri());
        assertEquals("CANDIDATE", first.getCode());
        assertEquals("candidate", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        ConceptStatusService conceptStatusService = new ConceptStatusService(
                new LocalConceptStatusHarvester("20200923-1"),
                conceptStatusRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        conceptStatusService.harvestAndSave(false);

        HarvestSettings settings =
                harvestSettingsRepository.findById(CONCEPT_STATUS.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200923-1", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        conceptStatusService = new ConceptStatusService(
                new LocalConceptStatusHarvester("20200924-0"),
                conceptStatusRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        conceptStatusService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(CONCEPT_STATUS.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200924-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        conceptStatusService = new ConceptStatusService(
                new LocalConceptStatusHarvester("20200923-0"),
                conceptStatusRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        conceptStatusService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(CONCEPT_STATUS.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200924-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        ConceptStatusRepository conceptStatusRepositorySpy = spy(this.conceptStatusRepository);

        ConceptStatus conceptStatus = ConceptStatus.builder()
                .uri("http://uri.no")
                .code("CONCEPT_STATUS")
                .label(Map.of("en", "My conceptStatus"))
                .build();
        conceptStatusRepositorySpy.save(conceptStatus);


        long count = conceptStatusRepositorySpy.count();
        assertTrue(count > 0);

        when(conceptStatusRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        ConceptStatusService conceptStatusService = new ConceptStatusService(
                new LocalConceptStatusHarvester("20200924-2"),
                conceptStatusRepositorySpy,
                rdfSourceRepository,
                harvestSettingsRepository);

        assertEquals(count, conceptStatusRepositorySpy.count());
    }
}
