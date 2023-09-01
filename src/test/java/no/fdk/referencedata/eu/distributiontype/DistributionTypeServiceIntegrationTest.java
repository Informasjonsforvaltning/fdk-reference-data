package no.fdk.referencedata.eu.distributiontype;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.eu.distributiontype.LocalDistributionTypeHarvester.DISTRIBUTION_TYPES_SIZE;
import static no.fdk.referencedata.settings.Settings.DISTRIBUTION_TYPE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class DistributionTypeServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private DistributionTypeRepository distributionTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Test
    public void test_if_harvest_persists_distribution_types() {
        DistributionTypeService accessRightService = new DistributionTypeService(
                new LocalDistributionTypeHarvester("20200923-0"),
                distributionTypeRepository,
                harvestSettingsRepository);

        accessRightService.harvestAndSave(false);

        final AtomicInteger counter = new AtomicInteger();
        distributionTypeRepository.findAll().forEach(accessRight -> counter.incrementAndGet());
        assertEquals(DISTRIBUTION_TYPES_SIZE, counter.get());

        final DistributionType first = distributionTypeRepository.findById("http://publications.europa.eu/resource/authority/distribution-type/DOWNLOADABLE_FILE").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/distribution-type/DOWNLOADABLE_FILE", first.getUri());
        assertEquals("DOWNLOADABLE_FILE", first.getCode());
        assertEquals("Downloadable file", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        DistributionTypeService accessRightService = new DistributionTypeService(
                new LocalDistributionTypeHarvester("20200923-1"),
                distributionTypeRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        accessRightService.harvestAndSave(false);

        HarvestSettings settings =
                harvestSettingsRepository.findById(DISTRIBUTION_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200923-1", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        accessRightService = new DistributionTypeService(
                new LocalDistributionTypeHarvester("20200924-0"),
                distributionTypeRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        accessRightService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(DISTRIBUTION_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200924-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        accessRightService = new DistributionTypeService(
                new LocalDistributionTypeHarvester("20200924-0"),
                distributionTypeRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        accessRightService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(DISTRIBUTION_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200924-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        DistributionTypeRepository distrubutionTypeRepositorySpy = spy(this.distributionTypeRepository);

        DistributionType distributionType = DistributionType.builder()
                .uri("http://uri.no")
                .code("DISTRIBUTION_TYPE_A")
                .label(Map.of("en", "My distribution type"))
                .build();
        distrubutionTypeRepositorySpy.save(distributionType);


        long count = distrubutionTypeRepositorySpy.count();
        assertTrue(count > 0);

        when(distrubutionTypeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        DistributionTypeService accessRightService = new DistributionTypeService(
                new LocalDistributionTypeHarvester("20200924-2"),
                distributionTypeRepository,
                harvestSettingsRepository);

        assertEquals(count, distrubutionTypeRepositorySpy.count());
    }
}
