package no.fdk.referencedata.eu.eurovoc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class EurovocService {

    private final EurovocHarvester eurovocHarvester;

    private final EurovocRepository eurovocRepository;

    private final EurovocSettingsRepository eurovocSettingsRepository;

    @Autowired
    public EurovocService(EurovocHarvester eurovocHarvester,
                          EurovocRepository eurovocRepository,
                          EurovocSettingsRepository eurovocSettingsRepository) {
        this.eurovocHarvester = eurovocHarvester;
        this.eurovocRepository = eurovocRepository;
        this.eurovocSettingsRepository = eurovocSettingsRepository;
    }

    @Transactional
    public void harvest() {
        try {
            final String version = eurovocHarvester.getVersion();
            final int versionIntValue = Integer.parseInt(eurovocHarvester.getVersion().replace("-", ""));

            final EurovocSettings settings = eurovocSettingsRepository.findById(EurovocSettings.SETTINGS)
                    .orElse(EurovocSettings.builder()
                            .id(EurovocSettings.SETTINGS)
                            .latestVersion("0")
                            .build());

            final int currentVersion = Integer.parseInt(settings.getLatestVersion().replace("-", ""));

            if(currentVersion < versionIntValue) {
                eurovocRepository.deleteAll();
                eurovocRepository.saveAll(eurovocHarvester.harvest().toIterable());

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(version);
                eurovocSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest eurovoc", e);
        }
    }
}
