package no.fdk.referencedata.digdir.evidencetype;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.settings.Settings.EVIDENCE_TYPE;
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

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_evidence_types() {
        EvidenceTypeService evidenceTypeService = new EvidenceTypeService(
                new LocalEvidenceTypeHarvester("123-0"),
                evidenceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        evidenceTypeService.harvestAndSave(false);

        final AtomicInteger counter = new AtomicInteger();
        evidenceTypeRepository.findAll().forEach(evidenceType -> counter.incrementAndGet());
        assertEquals(4, counter.get());

        final EvidenceType first = evidenceTypeRepository.findById("https://data.norge.no/vocabulary/evidence-type#declaration").orElseThrow();
        assertEquals("https://data.norge.no/vocabulary/evidence-type#declaration", first.getUri());
        assertEquals("declaration", first.getCode());
        assertEquals("declaration", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        EvidenceTypeService evidenceTypeService = new EvidenceTypeService(
                new LocalEvidenceTypeHarvester("132-0"),
                evidenceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        evidenceTypeService.harvestAndSave(false);

        HarvestSettings settings =
                harvestSettingsRepository.findById(EVIDENCE_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("132-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        evidenceTypeService = new EvidenceTypeService(
                new LocalEvidenceTypeHarvester("132-2"),
                evidenceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        evidenceTypeService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(EVIDENCE_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("132-2", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        evidenceTypeService = new EvidenceTypeService(
                new LocalEvidenceTypeHarvester("132-1"),
                evidenceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        evidenceTypeService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(EVIDENCE_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("132-2", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
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

        EvidenceTypeService evidenceTypeService = new EvidenceTypeService(
                new LocalEvidenceTypeHarvester("123-2"),
                evidenceTypeRepositorySpy,
                rdfSourceRepository,
                harvestSettingsRepository);

        assertEquals(count, evidenceTypeRepositorySpy.count());
    }
}
