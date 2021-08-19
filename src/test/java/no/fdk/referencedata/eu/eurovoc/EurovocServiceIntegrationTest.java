package no.fdk.referencedata.eu.eurovoc;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.mongo.AbstractMongoDbContainerTest;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.settings.Settings.EUROVOC;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = { "scheduling.enabled=false" })
public class EurovocServiceIntegrationTest extends AbstractMongoDbContainerTest {

    @Autowired
    private EurovocRepository eurovocRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Test
    public void test_if_harvest_persists_eurovoc() {
        EurovocService eurovocService = new EurovocService(
                new LocalEurovocHarvester("20200923-0"),
                eurovocRepository,
                harvestSettingsRepository);

        eurovocService.harvest();

        final AtomicInteger counter = new AtomicInteger();
        eurovocRepository.findAll().forEach(fileType -> counter.incrementAndGet());
        assertEquals(7322, counter.get());

        final Eurovoc eurovoc5548 = eurovocRepository.findById("http://eurovoc.europa.eu/5548").orElseThrow();
        assertEquals("http://eurovoc.europa.eu/5548", eurovoc5548.getUri());
        assertEquals("5548", eurovoc5548.getCode());
        assertEquals("interinstitutional cooperation (EU)", eurovoc5548.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        EurovocService eurovocService = new EurovocService(
                new LocalEurovocHarvester("20200923-1"),
                eurovocRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        eurovocService.harvest();

        HarvestSettings settings =
                harvestSettingsRepository.findById(EUROVOC.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200923-1", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        eurovocService = new EurovocService(
                new LocalEurovocHarvester("20200924-0"),
                eurovocRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        eurovocService.harvest();

        settings =
                harvestSettingsRepository.findById(EUROVOC.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200924-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        eurovocService = new EurovocService(
                new LocalEurovocHarvester("20200924-0"),
                eurovocRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        eurovocService.harvest();

        settings =
                harvestSettingsRepository.findById(EUROVOC.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200924-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        EurovocRepository eurovocRepositorySpy = spy(this.eurovocRepository);

        eurovocRepositorySpy.save(Eurovoc.builder()
                .uri("http://uri.no")
                .code("1111")
                .label(Map.of("en", "My eurovoc"))
                .build());


        long count = eurovocRepositorySpy.count();
        assertTrue(count > 0);

        when(eurovocRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        EurovocService EurovocService = new EurovocService(
                new LocalEurovocHarvester("20200924-2"),
                eurovocRepositorySpy,
                harvestSettingsRepository);

        assertEquals(count, eurovocRepositorySpy.count());
    }
}
