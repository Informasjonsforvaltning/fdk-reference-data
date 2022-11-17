package no.fdk.referencedata.digdir.servicechanneltype;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import no.fdk.referencedata.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class ServiceChannelTypeService {

    private final ServiceChannelTypeHarvester serviceChannelTypeHarvester;

    private final ServiceChannelTypeRepository serviceChannelTypeRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public ServiceChannelTypeService(ServiceChannelTypeHarvester serviceChannelTypeHarvester,
                                     ServiceChannelTypeRepository serviceChannelTypeRepository,
                                     HarvestSettingsRepository harvestSettingsRepository) {
        this.serviceChannelTypeHarvester = serviceChannelTypeHarvester;
        this.serviceChannelTypeRepository = serviceChannelTypeRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    public boolean firstTime() {
        return serviceChannelTypeRepository.count() == 0;
    }

    @Transactional
    public void harvestAndSave(boolean force) {
        try {
            final Version latestVersion = new Version(serviceChannelTypeHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.SERVICE_CHANNEL_TYPE.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.SERVICE_CHANNEL_TYPE.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if(force || latestVersion.compareTo(currentVersion) > 0) {
                serviceChannelTypeRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<ServiceChannelType> iterable = serviceChannelTypeHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} service-channel-types", counter.get());
                serviceChannelTypeRepository.saveAll(iterable);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(serviceChannelTypeHarvester.getVersion());
                harvestSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest service-channel-types", e);
        }
    }
}
