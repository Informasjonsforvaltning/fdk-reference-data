package no.fdk.referencedata.eu.accessright;

import no.fdk.referencedata.eu.datatheme.DataTheme;
import no.fdk.referencedata.eu.datatheme.DataThemeRepository;
import no.fdk.referencedata.eu.datatheme.DataThemeService;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.mongo.AbstractMongoDbContainerTest;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.settings.Settings.ACCESS_RIGHT;
import static no.fdk.referencedata.settings.Settings.DATA_THEME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = { "scheduling.enabled=false" })
public class AccessRightServiceIntegrationTest extends AbstractMongoDbContainerTest {

    @Autowired
    private AccessRightRepository accessRightRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Test
    public void test_if_harvest_persists_datathemes() {
        AccessRightService accessRightService = new AccessRightService(
                new LocalAccessRightHarvester("20200923-0"),
                accessRightRepository,
                harvestSettingsRepository);

        accessRightService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        accessRightRepository.findAll().forEach(accessRight -> counter.incrementAndGet());
        assertEquals(6, counter.get());

        final AccessRight first = accessRightRepository.findById("http://publications.europa.eu/resource/authority/access-right/CONFIDENTIAL").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/access-right/CONFIDENTIAL", first.getUri());
        assertEquals("CONFIDENTIAL", first.getCode());
        assertEquals("confidential", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        AccessRightService accessRightService = new AccessRightService(
                new LocalAccessRightHarvester("20200923-1"),
                accessRightRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        accessRightService.harvestAndSave();

        HarvestSettings settings =
                harvestSettingsRepository.findById(ACCESS_RIGHT.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200923-1", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        accessRightService = new AccessRightService(
                new LocalAccessRightHarvester("20200924-0"),
                accessRightRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        accessRightService.harvestAndSave();

        settings =
                harvestSettingsRepository.findById(ACCESS_RIGHT.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200924-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        accessRightService = new AccessRightService(
                new LocalAccessRightHarvester("20200924-0"),
                accessRightRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        accessRightService.harvestAndSave();

        settings =
                harvestSettingsRepository.findById(ACCESS_RIGHT.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200924-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        AccessRightRepository accessRightRepositorySpy = spy(this.accessRightRepository);

        accessRightRepositorySpy.save(AccessRight.builder()
                .uri("http://uri.no")
                .code("ACCESS_RIGHT")
                .label(Map.of("en", "My right"))
                .build());


        long count = accessRightRepositorySpy.count();
        assertTrue(count > 0);

        when(accessRightRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        AccessRightService accessRightService = new AccessRightService(
                new LocalAccessRightHarvester("20200924-2"),
                accessRightRepositorySpy,
                harvestSettingsRepository);

        assertEquals(count, accessRightRepositorySpy.count());
    }
}
