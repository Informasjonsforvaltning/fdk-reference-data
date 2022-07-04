package no.fdk.referencedata.eu.frequency;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.container.AbstractContainerTest;
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

import static no.fdk.referencedata.settings.Settings.FREQUENCY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class FrequencyServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private FrequencyRepository frequencyRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @BeforeEach
    public void setup() {
        FrequencyService frequencyService = new FrequencyService(
                new LocalFrequencyHarvester("1"),
                frequencyRepository,
                harvestSettingsRepository);

        frequencyService.harvestAndSave(true);
    }

    @Test
    public void test_if_harvest_persists_frequencies() {
        FrequencyService frequencyService = new FrequencyService(
                new LocalFrequencyHarvester("20200923-0"),
                frequencyRepository,
                harvestSettingsRepository);

        frequencyService.harvestAndSave(false);

        final AtomicInteger counter = new AtomicInteger();
        frequencyRepository.findAll().forEach(frequency -> counter.incrementAndGet());
        assertEquals(31, counter.get());

        final Frequency first = frequencyRepository.findById("http://publications.europa.eu/resource/authority/frequency/ANNUAL").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/frequency/ANNUAL", first.getUri());
        assertEquals("ANNUAL", first.getCode());
        assertEquals("annual", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        FrequencyService frequencyService = new FrequencyService(
                new LocalFrequencyHarvester("20200923-1"),
                frequencyRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        frequencyService.harvestAndSave(false);

        HarvestSettings settings =
                harvestSettingsRepository.findById(FREQUENCY.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200923-1", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        frequencyService = new FrequencyService(
                new LocalFrequencyHarvester("20200924-0"),
                frequencyRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        frequencyService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(FREQUENCY.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200924-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        frequencyService = new FrequencyService(
                new LocalFrequencyHarvester("20200923-0"),
                frequencyRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        frequencyService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(FREQUENCY.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200924-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        FrequencyRepository frequencyRepositorySpy = spy(this.frequencyRepository);

        Frequency frequency = Frequency.builder()
                .uri("http://uri.no")
                .code("FREQUENCY")
                .label(Map.of("en", "My frequency"))
                .build();
        frequencyRepositorySpy.save(frequency);


        long count = frequencyRepositorySpy.count();
        assertTrue(count > 0);

        when(frequencyRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        FrequencyService frequencyService = new FrequencyService(
                new LocalFrequencyHarvester("20200924-2"),
                frequencyRepositorySpy,
                harvestSettingsRepository);

        assertEquals(count, frequencyRepositorySpy.count());
    }
}
