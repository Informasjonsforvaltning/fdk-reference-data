package no.fdk.referencedata;

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

import javax.annotation.PostConstruct;

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "scheduling", name="enabled", havingValue="true", matchIfMissing = true)
public class Scheduler {

    @Autowired
    private MediaTypeRepository mediaTypeRepository;

    @Autowired
    private FileTypeRepository fileTypeRepository;

    @Autowired
    private MediaTypeService mediaTypeService;

    @Autowired
    private FileTypeService fileTypeService;

    /**
     * Run every day 02:00 (at night)
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateMediaTypes() {
        mediaTypeService.harvestAndSaveMediaTypes();
    }

    /**
     * Run every day 03:00 (at night)
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void updateFileTypes() {
        fileTypeService.harvestAndSaveFileTypes();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if(fileTypeRepository.count() == 0) {
            fileTypeService.harvestAndSaveFileTypes();
        }

        if(mediaTypeRepository.count() == 0) {
            mediaTypeService.harvestAndSaveMediaTypes();
        }
    }
}