package no.fdk.referencedata.eu.datatheme;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.accessright.AccessRight;
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
public class DataThemeService {

    private final DataThemeHarvester dataThemeHarvester;

    private final DataThemeRepository dataThemeRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public DataThemeService(DataThemeHarvester dataThemeHarvester,
                            DataThemeRepository dataThemeRepository,
                            HarvestSettingsRepository harvestSettingsRepository) {
        this.dataThemeHarvester = dataThemeHarvester;
        this.dataThemeRepository = dataThemeRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    @Transactional
    public void harvestAndSave(boolean force) {
        try {
            final Version latestVersion = new Version(dataThemeHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.DATA_THEME.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.DATA_THEME.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if(force || latestVersion.compareTo(currentVersion) > 0) {
                dataThemeRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<DataTheme> iterable = dataThemeHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} data-themes", counter.get());
                dataThemeRepository.saveAll(iterable);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(dataThemeHarvester.getVersion());
                harvestSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest data-themes", e);
        }
    }
}
