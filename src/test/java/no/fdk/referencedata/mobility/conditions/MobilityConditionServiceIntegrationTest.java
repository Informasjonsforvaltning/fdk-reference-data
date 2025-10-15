package no.fdk.referencedata.mobility.conditions;

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

import static no.fdk.referencedata.settings.Settings.MOBILITY_CONDITION;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class MobilityConditionServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private MobilityConditionRepository mobilityConditionRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_conditions() {
        MobilityConditionService mobilityConditionService = new MobilityConditionService(
                new LocalMobilityConditionHarvester("1.0.1"),
                mobilityConditionRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        mobilityConditionService.harvestAndSave(true);

        final AtomicInteger counter = new AtomicInteger();
        mobilityConditionRepository.findAll().forEach(condition -> counter.incrementAndGet());
        assertEquals(10, counter.get());

        final MobilityCondition first = mobilityConditionRepository.findById("https://w3id.org/mobilitydcat-ap/conditions-for-access-and-usage/fee-required").orElseThrow();
        assertEquals("https://w3id.org/mobilitydcat-ap/conditions-for-access-and-usage/fee-required", first.getUri());
        assertEquals("fee-required", first.getCode());
        assertEquals("Fee required", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        MobilityConditionService mobilityConditionService = new MobilityConditionService(
                new LocalMobilityConditionHarvester("1.1.1"),
                mobilityConditionRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        mobilityConditionService.harvestAndSave(false);

        HarvestSettings settings =
                harvestSettingsRepository.findById(MOBILITY_CONDITION.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("1.1.1", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        mobilityConditionService = new MobilityConditionService(
                new LocalMobilityConditionHarvester("1.1.2"),
                mobilityConditionRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        mobilityConditionService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(MOBILITY_CONDITION.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("1.1.2", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        mobilityConditionService = new MobilityConditionService(
                new LocalMobilityConditionHarvester("1.0.0"),
                mobilityConditionRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        mobilityConditionService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(MOBILITY_CONDITION.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("1.1.2", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        MobilityConditionRepository mobilityConditionRepositorySpy = spy(this.mobilityConditionRepository);

        MobilityCondition condition = MobilityCondition.builder()
                .uri("http://uri.no")
                .code("MOBILITY_CONDITION")
                .label(Map.of("en", "My condition"))
                .build();
        mobilityConditionRepositorySpy.save(condition);


        long count = mobilityConditionRepositorySpy.count();
        assertTrue(count > 0);

        when(mobilityConditionRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        MobilityConditionService mobilityConditionService = new MobilityConditionService(
                new LocalMobilityConditionHarvester("1.2.0"),
                mobilityConditionRepositorySpy,
                rdfSourceRepository,
                harvestSettingsRepository);

        assertEquals(count, mobilityConditionRepositorySpy.count());
    }
}
