package no.fdk.referencedata.eu.distributiontype;

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
public class DistributionTypeService {

    private final DistributionTypeHarvester distributionTypeHarvester;

    private final DistributionTypeRepository distributionTypeRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public DistributionTypeService(DistributionTypeHarvester distributionTypeHarvester,
                                   DistributionTypeRepository distributionTypeRepository,
                                   HarvestSettingsRepository harvestSettingsRepository) {
        this.distributionTypeHarvester = distributionTypeHarvester;
        this.distributionTypeRepository = distributionTypeRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    public boolean firstTime() {
        return distributionTypeRepository.count() == 0;
    }

    @Transactional
    public void harvestAndSave(boolean force) {
        try {
            final Version latestVersion = new Version(distributionTypeHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.DISTRIBUTION_TYPE.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.DISTRIBUTION_TYPE.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if(force || latestVersion.compareTo(currentVersion) > 0) {
                distributionTypeRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<DistributionType> iterable = distributionTypeHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} distribution-types", counter.get());
                distributionTypeRepository.saveAll(iterable);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(distributionTypeHarvester.getVersion());
                harvestSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest distribution-types", e);
        }
    }
}
