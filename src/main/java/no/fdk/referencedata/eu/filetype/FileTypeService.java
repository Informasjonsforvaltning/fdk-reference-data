package no.fdk.referencedata.eu.filetype;

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
    public void harvestAndSave(boolean force) {
        try {
            final Version latestVersion = new Version(fileTypeHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.FILE_TYPE.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.FILE_TYPE.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if(force || latestVersion.compareTo(currentVersion) > 0) {
                fileTypeRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<FileType> iterable = fileTypeHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} file-types", counter.get());
                fileTypeRepository.saveAll(iterable);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(fileTypeHarvester.getVersion());
                harvestSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest file-types", e);
        }
    }
}
