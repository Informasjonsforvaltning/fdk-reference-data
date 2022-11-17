package no.fdk.referencedata.digdir.servicechanneltype;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.settings.Settings.SERVICE_CHANNEL_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class ServiceChannelTypeServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ServiceChannelTypeRepository serviceChannelTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Test
    public void test_if_harvest_persists_datathemes() {
        ServiceChannelTypeService serviceChannelTypeService = new ServiceChannelTypeService(
                new LocalServiceChannelTypeHarvester("123-0"),
                serviceChannelTypeRepository,
                harvestSettingsRepository);

        serviceChannelTypeService.harvestAndSave(false);

        final AtomicInteger counter = new AtomicInteger();
        serviceChannelTypeRepository.findAll().forEach(serviceChannelType -> counter.incrementAndGet());
        assertEquals(11, counter.get());

        final ServiceChannelType first = serviceChannelTypeRepository.findById("https://data.norge.no/vocabulary/service-channel-type#service-bureau").orElseThrow();
        assertEquals("https://data.norge.no/vocabulary/service-channel-type#service-bureau", first.getUri());
        assertEquals("service-bureau", first.getCode());
        assertEquals("service bureau", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        ServiceChannelTypeService serviceChannelTypeService = new ServiceChannelTypeService(
                new LocalServiceChannelTypeHarvester("132-0"),
                serviceChannelTypeRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        serviceChannelTypeService.harvestAndSave(false);

        HarvestSettings settings =
                harvestSettingsRepository.findById(SERVICE_CHANNEL_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("132-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        serviceChannelTypeService = new ServiceChannelTypeService(
                new LocalServiceChannelTypeHarvester("132-2"),
                serviceChannelTypeRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        serviceChannelTypeService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(SERVICE_CHANNEL_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("132-2", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        serviceChannelTypeService = new ServiceChannelTypeService(
                new LocalServiceChannelTypeHarvester("132-1"),
                serviceChannelTypeRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        serviceChannelTypeService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(SERVICE_CHANNEL_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("132-2", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        ServiceChannelTypeRepository serviceChannelTypeRepositorySpy = spy(this.serviceChannelTypeRepository);

        ServiceChannelType serviceChannelType = ServiceChannelType.builder()
                .uri("http://uri.no")
                .code("SERVICE_CHANNEL_TYPE")
                .label(Map.of("en", "My channel"))
                .build();
        serviceChannelTypeRepositorySpy.save(serviceChannelType);


        long count = serviceChannelTypeRepositorySpy.count();
        assertTrue(count > 0);

        when(serviceChannelTypeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        ServiceChannelTypeService serviceChannelTypeService = new ServiceChannelTypeService(
                new LocalServiceChannelTypeHarvester("123-2"),
                serviceChannelTypeRepositorySpy,
                harvestSettingsRepository);

        assertEquals(count, serviceChannelTypeRepositorySpy.count());
    }
}
