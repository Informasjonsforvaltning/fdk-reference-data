package no.fdk.referencedata.digdir.legalresourcetype;

import no.fdk.referencedata.digdir.legalresourcetype.LegalResourceTypeWriter;
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
                new LocalLegalResourceTypeHarvester(),
                legalResourceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository,
                new LegalResourceTypeWriter(legalResourceTypeRepository, rdfSourceRepository, harvestSettingsRepository));

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
    public void test_if_harvest_always_persists_and_updates_version() {
        LegalResourceTypeService legalResourceTypeService = new LegalResourceTypeService(
                new LocalLegalResourceTypeHarvester(),
                legalResourceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository,
                new LegalResourceTypeWriter(legalResourceTypeRepository, rdfSourceRepository, harvestSettingsRepository));

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        legalResourceTypeService.harvestAndSave();

        HarvestSettings settings =
                harvestSettingsRepository.findById(LEGAL_RESOURCE_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        legalResourceTypeService = new LegalResourceTypeService(
                new LocalLegalResourceTypeHarvester(),
                legalResourceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository,
                new LegalResourceTypeWriter(legalResourceTypeRepository, rdfSourceRepository, harvestSettingsRepository));

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        legalResourceTypeService.harvestAndSave();

        settings =
                harvestSettingsRepository.findById(LEGAL_RESOURCE_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Same version
        legalResourceTypeService = new LegalResourceTypeService(
                new LocalLegalResourceTypeHarvester(),
                legalResourceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository,
                new LegalResourceTypeWriter(legalResourceTypeRepository, rdfSourceRepository, harvestSettingsRepository));

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        legalResourceTypeService.harvestAndSave();

        settings =
                harvestSettingsRepository.findById(LEGAL_RESOURCE_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertTrue(settings.getLatestHarvestDate().isAfter(thirdHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));
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
                harvestSettingsRepository,
                new LegalResourceTypeWriter(legalResourceTypeRepository, rdfSourceRepository, harvestSettingsRepository));

        assertEquals(count, legalResourceTypeRepositorySpy.count());
    }
}
