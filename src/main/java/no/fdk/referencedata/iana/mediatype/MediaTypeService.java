package no.fdk.referencedata.iana.mediatype;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.filetype.FileType;
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
public class MediaTypeService {

    private final MediaTypeHarvester mediaTypeHarvester;

    private final MediaTypeRepository mediaTypeRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public MediaTypeService(MediaTypeHarvester mediaTypeHarvester,
                            MediaTypeRepository mediaTypeRepository,
                            HarvestSettingsRepository harvestSettingsRepository) {
        this.mediaTypeHarvester = mediaTypeHarvester;
        this.mediaTypeRepository = mediaTypeRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    @Transactional
    public void harvestAndSave() {
        try {
            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.MEDIA_TYPE.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.MEDIA_TYPE.name())
                            .latestVersion("0")
                            .build());

            mediaTypeRepository.deleteAll();

            final AtomicInteger counter = new AtomicInteger(0);
            final Iterable<MediaType> iterable = mediaTypeHarvester.harvest().toIterable();
            iterable.forEach(item -> counter.getAndIncrement());
            log.info("Harvest and saving {} media-types", counter.get());
            mediaTypeRepository.saveAll(iterable);

            settings.setLatestHarvestDate(LocalDateTime.now());
            harvestSettingsRepository.save(settings);
        } catch(Exception e) {
            log.error("Unable to harvest media-types", e);
        }
    }
}
