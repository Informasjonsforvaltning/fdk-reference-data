package no.fdk.referencedata.digdir.legalresourcetype;

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

import static no.fdk.referencedata.digdir.legalresourcetype.LocalLegalResourceTypeHarvester.LEGAL_RESOURCE_TYPES_SIZE;
import static no.fdk.referencedata.settings.Settings.LEGAL_RESOURCE_TYPE;
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

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @BeforeEach
    public void setup() {
        legalResourceTypeRepository.deleteAll();
        harvestSettingsRepository.deleteAll();
    }

    @Test
    public void test_if_harvest_persists_legal_resource_types() {
        LegalResourceTypeService legalResourceTypeService = new LegalResourceTypeService(
                new LocalLegalResourceTypeHarvester("2023-08-17"),
                legalResourceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        legalResourceTypeService.harvestAndSave(false);

        final AtomicInteger counter = new AtomicInteger();
        legalResourceTypeRepository.findAll().forEach(legalResourceType -> counter.incrementAndGet());
        assertEquals(LEGAL_RESOURCE_TYPES_SIZE, counter.get());

        final LegalResourceType first = legalResourceTypeRepository.findById("https://data.norge.no/vocabulary/legal-resource-type#act").orElseThrow();
        assertEquals("https://data.norge.no/vocabulary/legal-resource-type#act", first.getUri());
        assertEquals("act", first.getCode());
        assertEquals("act", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        LegalResourceTypeService legalResourceTypeService = new LegalResourceTypeService(
                new LocalLegalResourceTypeHarvester("2023-08-17"),
                legalResourceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        legalResourceTypeService.harvestAndSave(false);
        LocalDateTime firstHarvestDateTime = LocalDateTime.now();

        HarvestSettings settings =
                harvestSettingsRepository.findById(LEGAL_RESOURCE_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("2023-08-17", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isBefore(firstHarvestDateTime));

        // Newer version
        legalResourceTypeService = new LegalResourceTypeService(
                new LocalLegalResourceTypeHarvester("2023-08-18"),
                legalResourceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        legalResourceTypeService.harvestAndSave(false);
        LocalDateTime secondHarvestDateTime = LocalDateTime.now();

        settings =
                harvestSettingsRepository.findById(LEGAL_RESOURCE_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("2023-08-18", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(secondHarvestDateTime));

        // Older version
        legalResourceTypeService = new LegalResourceTypeService(
                new LocalLegalResourceTypeHarvester("2023-08-16"),
                legalResourceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        legalResourceTypeService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(LEGAL_RESOURCE_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("2023-08-18", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(secondHarvestDateTime));
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

        LegalResourceTypeService legalResourceTypeService = new LegalResourceTypeService(
                new LocalLegalResourceTypeHarvester("2023-08-17"),
                legalResourceTypeRepositorySpy,
                rdfSourceRepository,
                harvestSettingsRepository);

        assertEquals(count, legalResourceTypeRepositorySpy.count());
    }
}
