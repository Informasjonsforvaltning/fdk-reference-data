package no.fdk.referencedata;

import no.fdk.referencedata.eu.accessright.AccessRightService;
import no.fdk.referencedata.eu.distributiontype.DistributionTypeService;
import no.fdk.referencedata.eu.datatheme.DataThemeService;
import no.fdk.referencedata.eu.eurovoc.EuroVocService;
import no.fdk.referencedata.eu.filetype.FileTypeService;
import no.fdk.referencedata.eu.frequency.FrequencyService;
import no.fdk.referencedata.iana.mediatype.MediaTypeService;
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
    private AccessRightService accessRightService;

    @Autowired
    private MediaTypeService mediaTypeService;

    @Autowired
    private FileTypeService fileTypeService;

    @Autowired
    private DataThemeService dataThemeService;

    @Autowired
    private EuroVocService euroVocService;

    @Autowired
    private FrequencyService frequencyService;

    @Autowired
    private DistributionTypeService distributionTypeService;

    /**
     * Run every day 01:30 (at night)
     */
    @Scheduled(cron = "0 30 1 * * ?")
    public void updateAccessRights() {
        accessRightService.harvestAndSave(false);
    }

    /**
     * Run every day 01:40 (at night)
     */
    @Scheduled(cron = "0 40 1 * * ?")
    public void updateMediaTypes() {
        mediaTypeService.harvestAndSave();
    }

    /**
     * Run every day 01:50 (at night)
     */
    @Scheduled(cron = "0 50 1 * * ?")
    public void updateFileTypes() {
        fileTypeService.harvestAndSave(false);
    }

    /**
     * Run every day 02:00 (at night)
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateDataThemes() {
        dataThemeService.harvestAndSave(false);
    }

    /**
     * Run every day 02:10 (at night)
     */
    @Scheduled(cron = "0 10 2 * * ?")
    public void updateEuroVoc() {
        euroVocService.harvestAndSave(false);
    }

    /**
     * Run every day 02:20 (at night)
     */
    @Scheduled(cron = "0 20 2 * * ?")
    public void updateFrequencies() {
        frequencyService.harvestAndSave(false);
    }

    /**
     * Run every day 02:30 (at night)
     */
    @Scheduled(cron = "0 30 2 * * ?")
    public void updateDistributionTypes() {
        distributionTypeService.harvestAndSave(false);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if(accessRightService.firstTime()) {
            accessRightService.harvestAndSave(true);
        }

        if(fileTypeService.firstTime()) {
            fileTypeService.harvestAndSave(true);
        }

        if(mediaTypeService.firstTime()) {
            mediaTypeService.harvestAndSave();
        }

        if(dataThemeService.firstTime()) {
            dataThemeService.harvestAndSave(true);
        }

        if(euroVocService.firstTime()) {
            euroVocService.harvestAndSave(true);
        }

        if(frequencyService.firstTime()) {
            frequencyService.harvestAndSave(true);
        }

        if(distributionTypeService.firstTime()) {
            distributionTypeService.harvestAndSave(true);
        }
    }
}
