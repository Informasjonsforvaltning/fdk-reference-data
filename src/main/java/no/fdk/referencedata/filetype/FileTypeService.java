package no.fdk.referencedata.filetype;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class FileTypeService {

    private final FileTypeHarvester fileTypeHarvester;

    private final FileTypeRepository fileTypeRepository;

    private final FileTypeSettingsRepository fileTypeSettingsRepository;

    @Autowired
    public FileTypeService(FileTypeHarvester fileTypeHarvester,
                           FileTypeRepository fileTypeRepository,
                           FileTypeSettingsRepository fileTypeSettingsRepository) {
        this.fileTypeHarvester = fileTypeHarvester;
        this.fileTypeRepository = fileTypeRepository;
        this.fileTypeSettingsRepository = fileTypeSettingsRepository;
    }

    @Transactional
    public void harvestAndSaveFileTypes() {
        try {
            final String version = fileTypeHarvester.getVersion();
            final int versionIntValue = Integer.parseInt(fileTypeHarvester.getVersion().replace("-", ""));

            final FileTypeSettings settings = fileTypeSettingsRepository.findById(FileTypeSettings.SETTINGS)
                    .orElse(FileTypeSettings.builder()
                            .id(FileTypeSettings.SETTINGS)
                            .latestVersion("0")
                            .build());

            final int currentVersion = Integer.parseInt(settings.getLatestVersion().replace("-", ""));

            if(currentVersion < versionIntValue) {
                fileTypeRepository.deleteAll();
                fileTypeRepository.saveAll(fileTypeHarvester.harvestFileTypes().toIterable());

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(version);
                fileTypeSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest file-types", e);
        }
    }
}
