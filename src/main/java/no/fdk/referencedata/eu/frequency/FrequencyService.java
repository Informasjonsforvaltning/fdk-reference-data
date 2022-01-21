package no.fdk.referencedata.eu.frequency;

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
public class FrequencyService {

    private final FrequencyHarvester frequencyHarvester;

    private final FrequencyRepository frequencyRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public FrequencyService(FrequencyHarvester frequencyHarvester,
                            FrequencyRepository frequencyRepository,
                            HarvestSettingsRepository harvestSettingsRepository) {
        this.frequencyHarvester = frequencyHarvester;
        this.frequencyRepository = frequencyRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    public boolean firstTime() {
        return frequencyRepository.count() == 0;
    }

    @Transactional
    public void harvestAndSave(boolean force) {
        try {
            final Version latestVersion = new Version(frequencyHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.FREQUENCY.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.FREQUENCY.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if(force || latestVersion.compareTo(currentVersion) > 0) {
                frequencyRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<Frequency> iterable = frequencyHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} frequencies", counter.get());
                frequencyRepository.saveAll(iterable);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(frequencyHarvester.getVersion());
                harvestSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest frequencies", e);
        }
    }
}
