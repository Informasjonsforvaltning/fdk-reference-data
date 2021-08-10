package no.fdk.referencedata;

import no.fdk.referencedata.datatheme.DataThemeRepository;
import no.fdk.referencedata.datatheme.DataThemeService;
import no.fdk.referencedata.filetype.FileTypeRepository;
import no.fdk.referencedata.filetype.FileTypeService;
import no.fdk.referencedata.mediatype.MediaTypeRepository;
import no.fdk.referencedata.mediatype.MediaTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "scheduling", name="enabled", havingValue="true", matchIfMissing = true)
public class Scheduler {

    @Autowired
    private MediaTypeRepository mediaTypeRepository;

    @Autowired
    private FileTypeRepository fileTypeRepository;

    @Autowired
    private DataThemeRepository dataThemeRepository;

    @Autowired
    private MediaTypeService mediaTypeService;

    @Autowired
    private FileTypeService fileTypeService;

    @Autowired
    private DataThemeService dataThemeService;

    /**
     * Run every day 02:00 (at night)
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateMediaTypes() {
        mediaTypeService.harvestAndSaveMediaTypes();
    }

    /**
     * Run every day 02:30 (at night)
     */
    @Scheduled(cron = "0 30 2 * * ?")
    public void updateFileTypes() {
        fileTypeService.harvestAndSaveFileTypes();
    }

    /**
     * Run every day 03:00 (at night)
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void updateDataThemes() {
        dataThemeService.harvestAndSaveDataThemes();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if(fileTypeRepository.count() == 0) {
            fileTypeService.harvestAndSaveFileTypes();
        }

        if(mediaTypeRepository.count() == 0) {
            mediaTypeService.harvestAndSaveMediaTypes();
        }

        if(dataThemeRepository.count() == 0) {
            dataThemeService.harvestAndSaveDataThemes();
        }
    }
}
