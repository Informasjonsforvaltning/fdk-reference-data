package no.fdk.referencedata.eu.filetype;

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
public class FileTypeService {

    private final FileTypeHarvester fileTypeHarvester;

    private final FileTypeRepository fileTypeRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public FileTypeService(FileTypeHarvester fileTypeHarvester,
                           FileTypeRepository fileTypeRepository,
                           HarvestSettingsRepository harvestSettingsRepository) {
        this.fileTypeHarvester = fileTypeHarvester;
        this.fileTypeRepository = fileTypeRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    @Transactional
    public void harvestAndSaveFileTypes() {
        try {
            final String version = fileTypeHarvester.getVersion();
            final int versionIntValue = Integer.parseInt(fileTypeHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.FILE_TYPE.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.FILE_TYPE.name())
                            .latestVersion("0")
                            .build());

            final int currentVersion = Integer.parseInt(settings.getLatestVersion().replace("-", ""));

            if(currentVersion < versionIntValue) {
                fileTypeRepository.deleteAll();
                fileTypeRepository.saveAll(fileTypeHarvester.harvestFileTypes().toIterable());

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(version);
                harvestSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest file-types", e);
        }
    }
}
