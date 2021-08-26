package no.fdk.referencedata.eu.filetype;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

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
    public void harvestAndSave() {
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

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<FileType> iterable = fileTypeHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} file-types", counter.get());
                fileTypeRepository.saveAll(iterable);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(version);
                harvestSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest file-types", e);
        }
    }
}
