package no.fdk.referencedata.digdir.audiencetype;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.digdir.audiencetype.AudienceType;
import no.fdk.referencedata.digdir.audiencetype.AudienceTypeRepository;
import no.fdk.referencedata.digdir.audiencetype.AudienceTypeService;
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

import static no.fdk.referencedata.settings.Settings.AUDIENCE_TYPE;
import static no.fdk.referencedata.settings.Settings.EVIDENCE_TYPE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class AudienceTypeServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private AudienceTypeRepository audienceTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_audience_types() {
        AudienceTypeService audienceTypeService = new AudienceTypeService(
                new LocalAudienceTypeHarvester("123-0"),
                audienceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        audienceTypeService.harvestAndSave(false);

        final AtomicInteger counter = new AtomicInteger();
        audienceTypeRepository.findAll().forEach(audienceType -> counter.incrementAndGet());
        assertEquals(2, counter.get());

        final AudienceType first = audienceTypeRepository.findById("https://data.norge.no/vocabulary/audience-type#public").orElseThrow();
        assertEquals("https://data.norge.no/vocabulary/audience-type#public", first.getUri());
        assertEquals("public", first.getCode());
        assertEquals("public", first.getLabel().get(Language.ENGLISH.code()));
        assertEquals("allmenta", first.getLabel().get(Language.NORWEGIAN_NYNORSK.code()));
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        AudienceTypeService audienceTypeService = new AudienceTypeService(
                new LocalAudienceTypeHarvester("132-0"),
                audienceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        audienceTypeService.harvestAndSave(false);

        HarvestSettings settings =
                harvestSettingsRepository.findById(AUDIENCE_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("132-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        audienceTypeService = new AudienceTypeService(
                new LocalAudienceTypeHarvester("132-2"),
                audienceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        audienceTypeService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(AUDIENCE_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("132-2", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        audienceTypeService = new AudienceTypeService(
                new LocalAudienceTypeHarvester("132-1"),
                audienceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        audienceTypeService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(AUDIENCE_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("132-2", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        AudienceTypeRepository audienceTypeRepositorySpy = spy(this.audienceTypeRepository);

        AudienceType audienceType = AudienceType.builder()
                .uri("http://uri.no")
                .code("AUDIENCE_TYPE")
                .label(Map.of("en", "My audience"))
                .build();
        audienceTypeRepositorySpy.save(audienceType);


        long count = audienceTypeRepositorySpy.count();
        assertTrue(count > 0);

        when(audienceTypeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        AudienceTypeService audienceTypeService = new AudienceTypeService(
                new LocalAudienceTypeHarvester("123-2"),
                audienceTypeRepositorySpy,
                rdfSourceRepository,
                harvestSettingsRepository);

        assertEquals(count, audienceTypeRepositorySpy.count());
    }
}
