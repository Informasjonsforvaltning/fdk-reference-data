package no.fdk.referencedata.eu.eurovoc;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.datatheme.DataTheme;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class EurovocService {

    private final EurovocHarvester eurovocHarvester;

    private final EurovocRepository eurovocRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public EurovocService(EurovocHarvester eurovocHarvester,
                          EurovocRepository eurovocRepository,
                          HarvestSettingsRepository harvestSettingsRepository) {
        this.eurovocHarvester = eurovocHarvester;
        this.eurovocRepository = eurovocRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    @Transactional
    public void harvestAndSaveEurovoc() {
        try {
            final String version = eurovocHarvester.getVersion();
            final int versionIntValue = Integer.parseInt(eurovocHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.EUROVOC.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.EUROVOC.name())
                            .latestVersion("0")
                            .build());

            final int currentVersion = Integer.parseInt(settings.getLatestVersion().replace("-", ""));

            if(currentVersion < versionIntValue) {
                eurovocRepository.deleteAll();
                eurovocRepository.saveAll(eurovocHarvester.harvest().toIterable());

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(version);
                harvestSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest eurovoc", e);
        }
    }
}
