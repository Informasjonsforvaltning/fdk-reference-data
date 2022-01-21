package no.fdk.referencedata.eu.accessright;

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
public class AccessRightService {

    private final AccessRightHarvester accessRightHarvester;

    private final AccessRightRepository accessRightRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public AccessRightService(AccessRightHarvester accessRightHarvester,
                              AccessRightRepository accessRightRepository,
                              HarvestSettingsRepository harvestSettingsRepository) {
        this.accessRightHarvester = accessRightHarvester;
        this.accessRightRepository = accessRightRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    public boolean firstTime() {
        return accessRightRepository.count() == 0;
    }

    @Transactional
    public void harvestAndSave(boolean force) {
        try {
            final Version latestVersion = new Version(accessRightHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.ACCESS_RIGHT.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.ACCESS_RIGHT.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if(force || latestVersion.compareTo(currentVersion) > 0) {
                accessRightRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<AccessRight> iterable = accessRightHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} access-rights", counter.get());
                accessRightRepository.saveAll(iterable);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(accessRightHarvester.getVersion());
                harvestSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest access-rights", e);
        }
    }
}
