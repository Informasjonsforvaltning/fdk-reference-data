package no.fdk.referencedata.config;

import no.fdk.referencedata.filetype.FileTypeService;
import no.fdk.referencedata.mediatype.MediaTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "scheduling", name="enabled", havingValue="true", matchIfMissing = true)
public class SchedulingConfiguration {

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
}