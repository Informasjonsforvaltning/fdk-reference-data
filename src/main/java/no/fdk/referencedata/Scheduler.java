package no.fdk.referencedata;

import no.fdk.referencedata.digdir.relationshipwithsourcetype.RelationshipWithSourceTypeService;
import no.fdk.referencedata.digdir.audiencetype.AudienceTypeService;
import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubjectService;
import no.fdk.referencedata.digdir.evidencetype.EvidenceTypeService;
import no.fdk.referencedata.digdir.roletype.RoleTypeService;
import no.fdk.referencedata.digdir.servicechanneltype.ServiceChannelTypeService;
import no.fdk.referencedata.eu.accessright.AccessRightService;
import no.fdk.referencedata.eu.conceptstatus.ConceptStatusService;
import no.fdk.referencedata.eu.datasettype.DatasetTypeService;
import no.fdk.referencedata.eu.distributionstatus.DistributionStatusService;
import no.fdk.referencedata.eu.distributiontype.DistributionTypeService;
import no.fdk.referencedata.eu.datatheme.DataThemeService;
import no.fdk.referencedata.eu.eurovoc.EuroVocService;
import no.fdk.referencedata.eu.filetype.FileTypeService;
import no.fdk.referencedata.eu.frequency.FrequencyService;
import no.fdk.referencedata.eu.mainactivity.MainActivityService;
import no.fdk.referencedata.geonorge.administrativeenheter.EnhetService;
import no.fdk.referencedata.iana.mediatype.MediaTypeService;
import no.fdk.referencedata.los.LosService;
import no.fdk.referencedata.ssb.fylkeorganisasjoner.FylkeOrganisasjonService;
import no.fdk.referencedata.ssb.kommuneorganisasjoner.KommuneOrganisasjonService;
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
    private AudienceTypeService audienceTypeService;

    @Autowired
    private RelationshipWithSourceTypeService relationshipWithSourceTypeService;

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
    private ConceptStatusService conceptStatusService;

    @Autowired
    private DistributionStatusService distributionStatusService;

    @Autowired
    private DistributionTypeService distributionTypeService;

    @Autowired
    private DatasetTypeService datasetTypeService;

    @Autowired
    private EnhetService enhetService;

    @Autowired
    private MainActivityService mainActivityService;

    @Autowired
    private RoleTypeService roleTypeService;

    @Autowired
    private ServiceChannelTypeService serviceChannelTypeService;

    @Autowired
    private ConceptSubjectService conceptSubjectService;

    @Autowired
    private EvidenceTypeService evidenceTypeService;

    @Autowired
    private LosService losService;

    @Autowired
    private FylkeOrganisasjonService fylkeOrganisasjonService;

    @Autowired
    private KommuneOrganisasjonService kommuneOrganisasjonService;

    /**
     * Run every hour
     */
    @Scheduled(cron = "0 45 * * * ?")
    public void updateConceptSubjects() {
        conceptSubjectService.harvestAndSave();
    }

    /**
     * Run every day 01:00 (at night)
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void updateEvidenceTypes() {
        evidenceTypeService.harvestAndSave(false);
    }

    /**
     * Run every day 01:10 (at night)
     */
    @Scheduled(cron = "0 10 1 * * ?")
    public void updateServiceChannelTypes() {
        serviceChannelTypeService.harvestAndSave(false);
    }

    /**
     * Run every day 01:20 (at night)
     */
    @Scheduled(cron = "0 20 1 * * ?")
    public void updateRoleTypes() {
        roleTypeService.harvestAndSave(false);
    }

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
     * Run every day 02:25 (at night)
     */
    @Scheduled(cron = "0 25 2 * * ?")
    public void updateDistributionStatuses() {
        distributionStatusService.harvestAndSave(false);
    }

    /**
     * Run every day 02:30 (at night)
     */
    @Scheduled(cron = "0 30 2 * * ?")
    public void updateDistributionTypes() {
        distributionTypeService.harvestAndSave(false);
    }

    /**
     * Run every day 02:35 (at night)
     */
    @Scheduled(cron = "0 35 2 * * ?")
    public void updateDatasetTypes() {
        datasetTypeService.harvestAndSave(false);
    }

    /**
     * Run every day 02:40 (at night)
     */
    @Scheduled(cron = "0 40 2 * * ?")
    public void updateMainActivities() {
        mainActivityService.harvestAndSave(false);
    }

    /**
     * Run every day 02:50 (at night)
     */
    @Scheduled(cron = "0 50 2 * * ?")
    public void updateLos() {
        losService.importLosNodes();
    }

    /**
     * Run every day 03:10 (at night)
     */
    @Scheduled(cron = "0 10 3 * * ?")
    public void updateFylkeskommuner() {
        fylkeOrganisasjonService.harvestAndSave();
    }

    /**
     * Run every day 03:20 (at night)
     */
    @Scheduled(cron = "0 20 3 * * ?")
    public void updateKommuneOrganisasjoner() {
        kommuneOrganisasjonService.harvestAndSave();
    }

    /**
     * Run every day 03:40 (at night)
     */
    @Scheduled(cron = "0 40 3 * * ?")
    public void updateConceptStatuses() {
        conceptStatusService.harvestAndSave(false);
    }

    /**
     * Run every day 03:50 (at night)
     */
    @Scheduled(cron = "0 50 3 * * ?")
    public void updateAudienceTypes() {
        audienceTypeService.harvestAndSave(false);
    }

    /**
     * Run every day 04:00 (at night)
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void updateAdministrativeEnheter() {
        enhetService.harvestAndSave();
    }

    @Scheduled(cron = "0 50 3 * * ?")
    public void updateRelationshipWithSourceTypes() {
        relationshipWithSourceTypeService.harvestAndSave(false);
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

        if(datasetTypeService.firstTime()) {
            datasetTypeService.harvestAndSave(true);
        }

        if(enhetService.firstTime()) {
            enhetService.harvestAndSave();
        }

        if(mainActivityService.firstTime()) {
            mainActivityService.harvestAndSave(true);
        }

        if(roleTypeService.firstTime()) {
            roleTypeService.harvestAndSave(true);
        }

        if(serviceChannelTypeService.firstTime()) {
            serviceChannelTypeService.harvestAndSave(true);
        }

        if(conceptSubjectService.firstTime()) {
            conceptSubjectService.harvestAndSave();
        }

        if(evidenceTypeService.firstTime()) {
            evidenceTypeService.harvestAndSave(true);
        }

        if(losService.firstTime()) {
            losService.importLosNodes();
        }

        if(fylkeOrganisasjonService.firstTime()) {
            fylkeOrganisasjonService.harvestAndSave();
        }

        if(kommuneOrganisasjonService.firstTime()) {
            kommuneOrganisasjonService.harvestAndSave();
        }

        if(conceptStatusService.firstTime()) {
            conceptStatusService.harvestAndSave(true);
        }

        if(audienceTypeService.firstTime()) {
            audienceTypeService.harvestAndSave(true);
        }

        if(relationshipWithSourceTypeService.firstTime()) {
            relationshipWithSourceTypeService.harvestAndSave(true);
        }
    }
}
