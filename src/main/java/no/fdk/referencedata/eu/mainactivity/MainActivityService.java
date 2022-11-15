package no.fdk.referencedata.eu.mainactivity;

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
public class MainActivityService {

    private final MainActivityHarvester mainActivityHarvester;

    private final MainActivityRepository mainActivityRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public MainActivityService(MainActivityHarvester mainActivityHarvester,
                               MainActivityRepository mainActivityRepository,
                               HarvestSettingsRepository harvestSettingsRepository) {
        this.mainActivityHarvester = mainActivityHarvester;
        this.mainActivityRepository = mainActivityRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    public boolean firstTime() {
        return mainActivityRepository.count() == 0;
    }

    @Transactional
    public void harvestAndSave(boolean force) {
        try {
            final Version latestVersion = new Version(mainActivityHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.MAIN_ACTIVITY.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.MAIN_ACTIVITY.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if(force || latestVersion.compareTo(currentVersion) > 0) {
                mainActivityRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<MainActivity> iterable = mainActivityHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} main-activities", counter.get());
                mainActivityRepository.saveAll(iterable);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(mainActivityHarvester.getVersion());
                harvestSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest main-activities", e);
        }
    }
}
