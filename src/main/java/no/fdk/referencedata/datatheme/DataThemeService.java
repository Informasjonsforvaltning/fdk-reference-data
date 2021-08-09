package no.fdk.referencedata.datatheme;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class DataThemeService {

    private final DataThemeHarvester dataThemeHarvester;

    private final DataThemeRepository dataThemeRepository;

    private final DataThemeSettingsRepository dataThemeSettingsRepository;

    @Autowired
    public DataThemeService(DataThemeHarvester dataThemeHarvester,
                            DataThemeRepository dataThemeRepository,
                            DataThemeSettingsRepository dataThemeSettingsRepository) {
        this.dataThemeHarvester = dataThemeHarvester;
        this.dataThemeRepository = dataThemeRepository;
        this.dataThemeSettingsRepository = dataThemeSettingsRepository;
    }

    @Transactional
    public void harvestAndSaveDataThemes() {
        try {
            final String version = dataThemeHarvester.getVersion();
            final int versionIntValue = Integer.parseInt(dataThemeHarvester.getVersion().replace("-", ""));

            final DataThemeSettings settings = dataThemeSettingsRepository.findById(DataThemeSettings.SETTINGS)
                    .orElse(DataThemeSettings.builder()
                            .id(DataThemeSettings.SETTINGS)
                            .latestVersion("0")
                            .build());

            final int currentVersion = Integer.parseInt(settings.getLatestVersion().replace("-", ""));

            if(currentVersion < versionIntValue) {
                dataThemeRepository.deleteAll();
                dataThemeRepository.saveAll(dataThemeHarvester.harvestDataThemes().toIterable());

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(version);
                dataThemeSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest data-themes", e);
        }
    }
}
