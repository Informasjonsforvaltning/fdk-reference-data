package no.fdk.referencedata;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.digdir.audiencetype.AudienceTypeService;
import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubjectService;
import no.fdk.referencedata.digdir.evidencetype.EvidenceTypeService;
import no.fdk.referencedata.digdir.legalresourcetype.LegalResourceTypeService;
import no.fdk.referencedata.digdir.qualitydimension.QualityDimensionService;
import no.fdk.referencedata.digdir.relationshipwithsourcetype.RelationshipWithSourceTypeService;
import no.fdk.referencedata.digdir.roletype.RoleTypeService;
import no.fdk.referencedata.digdir.servicechanneltype.ServiceChannelTypeService;
import no.fdk.referencedata.eu.accessright.AccessRightService;
import no.fdk.referencedata.eu.conceptstatus.ConceptStatusService;
import no.fdk.referencedata.eu.continent.ContinentService;
import no.fdk.referencedata.eu.country.CountryService;
import no.fdk.referencedata.eu.currency.CurrencyService;
import no.fdk.referencedata.eu.datasettype.DatasetTypeService;
import no.fdk.referencedata.eu.datatheme.DataThemeService;
import no.fdk.referencedata.eu.distributionstatus.DistributionStatusService;
import no.fdk.referencedata.eu.distributiontype.DistributionTypeService;
import no.fdk.referencedata.eu.eurovoc.EuroVocService;
import no.fdk.referencedata.eu.filetype.FileTypeService;
import no.fdk.referencedata.eu.frequency.FrequencyService;
import no.fdk.referencedata.eu.highvaluecategories.HighValueCategoryService;
import no.fdk.referencedata.eu.language.LanguageService;
import no.fdk.referencedata.eu.licence.LicenceService;
import no.fdk.referencedata.eu.mainactivity.MainActivityService;
import no.fdk.referencedata.eu.plannedavailability.PlannedAvailabilityService;
import no.fdk.referencedata.geonames.GeonamesService;
import no.fdk.referencedata.geonorge.administrativeenheter.EnhetService;
import no.fdk.referencedata.iana.mediatype.MediaTypeService;
import no.fdk.referencedata.los.LosService;
import no.fdk.referencedata.mobility.conditions.MobilityConditionService;
import no.fdk.referencedata.mobility.datastandard.MobilityDataStandardService;
import no.fdk.referencedata.mobility.theme.MobilityThemeService;
import no.fdk.referencedata.ssb.fylkeorganisasjoner.FylkeOrganisasjonService;
import no.fdk.referencedata.ssb.kommuneorganisasjoner.KommuneOrganisasjonService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Scheduler {

    private final AccessRightService accessRightService;
    private final AudienceTypeService audienceTypeService;
    private final ConceptStatusService conceptStatusService;
    private final ConceptSubjectService conceptSubjectService;
    private final ContinentService continentService;
    private final CountryService countryService;
    private final CurrencyService currencyService;
    private final DataThemeService dataThemeService;
    private final DatasetTypeService datasetTypeService;
    private final DistributionStatusService distributionStatusService;
    private final DistributionTypeService distributionTypeService;
    private final EnhetService enhetService;
    private final EuroVocService euroVocService;
    private final EvidenceTypeService evidenceTypeService;
    private final FileTypeService fileTypeService;
    private final FrequencyService frequencyService;
    private final FylkeOrganisasjonService fylkeOrganisasjonService;
    private final GeonamesService geonamesService;
    private final HighValueCategoryService highValueCategoryService;
    private final KommuneOrganisasjonService kommuneOrganisasjonService;
    private final LanguageService languageService;
    private final LegalResourceTypeService legalResourceTypeService;
    private final LicenceService licenceService;
    private final LosService losService;
    private final MainActivityService mainActivityService;
    private final MediaTypeService mediaTypeService;
    private final MobilityConditionService mobilityConditionService;
    private final MobilityDataStandardService mobilityDataStandardService;
    private final MobilityThemeService mobilityThemeService;
    private final PlannedAvailabilityService plannedAvailabilityService;
    private final QualityDimensionService qualityDimensionService;
    private final RelationshipWithSourceTypeService relationshipWithSourceTypeService;
    private final RoleTypeService roleTypeService;
    private final ServiceChannelTypeService serviceChannelTypeService;

    /** Run every hour at minute 45. */
    @Scheduled(cron = "0 45 * * * ?")
    public void updateConceptSubjects() {
        conceptSubjectService.harvestAndSave();
    }

    /** Run once a month at 01:00 on the 1st. */
    @Scheduled(cron = "0 0 1 1 * ?")
    public void updateEvidenceTypes() {
        evidenceTypeService.harvestAndSave();
    }

    /** Run once a month at 01:10 on the 1st. */
    @Scheduled(cron = "0 10 1 1 * ?")
    public void updateServiceChannelTypes() {
        serviceChannelTypeService.harvestAndSave();
    }

    /** Run once a month at 01:20 on the 1st. */
    @Scheduled(cron = "0 20 1 1 * ?")
    public void updateRoleTypes() {
        roleTypeService.harvestAndSave();
    }

    /** Run once a month at 01:30 on the 1st. */
    @Scheduled(cron = "0 30 1 1 * ?")
    public void updateAccessRights() {
        accessRightService.harvestAndSave();
    }

    /** Run once a month at 01:40 on the 1st. */
    @Scheduled(cron = "0 40 1 1 * ?")
    public void updateMediaTypes() {
        mediaTypeService.harvestAndSave();
    }

    /** Run once a month at 01:50 on the 1st. */
    @Scheduled(cron = "0 50 1 1 * ?")
    public void updateFileTypes() {
        fileTypeService.harvestAndSave();
    }

    /** Run once a month at 02:00 on the 1st. */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void updateDataThemes() {
        dataThemeService.harvestAndSave();
    }

    /** Run once a month at 02:10 on the 1st. */
    @Scheduled(cron = "0 10 2 1 * ?")
    public void updateEuroVoc() {
        euroVocService.harvestAndSave();
    }

    /** Run once a month at 02:20 on the 1st. */
    @Scheduled(cron = "0 20 2 1 * ?")
    public void updateFrequencies() {
        frequencyService.harvestAndSave();
    }

    /** Run once a month at 02:25 on the 1st. */
    @Scheduled(cron = "0 25 2 1 * ?")
    public void updateDistributionStatuses() {
        distributionStatusService.harvestAndSave();
    }

    /** Run once a month at 02:30 on the 1st. */
    @Scheduled(cron = "0 30 2 1 * ?")
    public void updateDistributionTypes() {
        distributionTypeService.harvestAndSave();
    }

    /** Run once a month at 02:35 on the 1st. */
    @Scheduled(cron = "0 35 2 1 * ?")
    public void updateDatasetTypes() {
        datasetTypeService.harvestAndSave();
    }

    /** Run once a month at 02:40 on the 1st. */
    @Scheduled(cron = "0 40 2 1 * ?")
    public void updateMainActivities() {
        mainActivityService.harvestAndSave();
    }

    /** Run once a month at 02:50 on the 1st. */
    @Scheduled(cron = "0 50 2 1 * ?")
    public void updateLos() {
        losService.importLosNodes();
    }

    /** Run once a month at 03:10 on the 1st. */
    @Scheduled(cron = "0 10 3 1 * ?")
    public void updateFylkeskommuner() {
        fylkeOrganisasjonService.harvestAndSave();
    }

    /** Run once a month at 03:20 on the 1st. */
    @Scheduled(cron = "0 20 3 1 * ?")
    public void updateKommuneOrganisasjoner() {
        kommuneOrganisasjonService.harvestAndSave();
    }

    /** Run once a month at 03:40 on the 1st. */
    @Scheduled(cron = "0 40 3 1 * ?")
    public void updateConceptStatuses() {
        conceptStatusService.harvestAndSave();
    }

    /** Run once a month at 03:50 on the 1st. */
    @Scheduled(cron = "0 50 3 1 * ?")
    public void updateAudienceTypes() {
        audienceTypeService.harvestAndSave();
    }

    /** Run once a month at 04:00 on the 1st. */
    @Scheduled(cron = "0 0 4 1 * ?")
    public void updateAdministrativeEnheter() {
        enhetService.harvestAndSave();
    }

    /** Run once a month at 04:10 on the 1st. */
    @Scheduled(cron = "0 10 4 1 * ?")
    public void updateRelationshipWithSourceTypes() {
        relationshipWithSourceTypeService.harvestAndSave();
    }

    /** Run once a month at 04:20 on the 1st. */
    @Scheduled(cron = "0 20 4 1 * ?")
    public void updatePlannedAvailability() {
        plannedAvailabilityService.harvestAndSave();
    }

    /** Run once a month at 04:30 on the 1st. */
    @Scheduled(cron = "0 30 4 1 * ?")
    public void updateCurrencies() {
        currencyService.harvestAndSave();
    }

    /** Run once a month at 04:40 on the 1st. */
    @Scheduled(cron = "0 40 4 1 * ?")
    public void updateLicences() {
        licenceService.harvestAndSave();
    }

    /** Run once a month at 04:50 on the 1st. */
    @Scheduled(cron = "0 50 4 1 * ?")
    public void updateMobilityThemes() {
        mobilityThemeService.harvestAndSave();
    }

    /** Run once a month at 04:55 on the 1st. */
    @Scheduled(cron = "0 55 4 1 * ?")
    public void updateMobilityCondition() {
        mobilityConditionService.harvestAndSave();
    }

    /** Run once a month at 05:00 on the 1st. */
    @Scheduled(cron = "0 0 5 1 * ?")
    public void updateMobilityDataStandards() {
        mobilityDataStandardService.harvestAndSave();
    }

    /** Run once a month at 05:05 on the 1st. */
    @Scheduled(cron = "0 5 5 1 * ?")
    public void updateHighValueCategories() {
        highValueCategoryService.harvestAndSave();
    }

    /** Run once a month at 05:10 on the 1st. */
    @Scheduled(cron = "0 10 5 1 * ?")
    public void updateQualityDimensions() {
        qualityDimensionService.harvestAndSave();
    }

    /** Run once a month at 05:15 on the 1st. */
    @Scheduled(cron = "0 15 5 1 * ?")
    public void updateLegalResourceTypes() {
        legalResourceTypeService.harvestAndSave();
    }

    /** Run once a month at 05:20 on the 1st. */
    @Scheduled(cron = "0 20 5 1 * ?")
    public void updateContinents() {
        continentService.harvestAndSave();
    }

    /** Run once a month at 05:25 on the 1st. */
    @Scheduled(cron = "0 25 5 1 * ?")
    public void updateCountries() {
        countryService.harvestAndSave();
    }

    /** Run once a month at 05:30 on the 1st. */
    @Scheduled(cron = "0 30 5 1 * ?")
    public void updateGeonames() {
        geonamesService.harvestAndSave();
    }

    /**
     * Run every day 05:35 (at night)
     */
    @Scheduled(cron = "0 35 5 * * ?")
    public void updateLanguages() {
        languageService.harvestAndSave();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (accessRightService.firstTime()) {
            accessRightService.harvestAndSave();
        }
        if (audienceTypeService.firstTime()) {
            audienceTypeService.harvestAndSave();
        }
        if (conceptStatusService.firstTime()) {
            conceptStatusService.harvestAndSave();
        }
        if (conceptSubjectService.firstTime()) {
            conceptSubjectService.harvestAndSave();
        }
        if (continentService.firstTime()) {
            continentService.harvestAndSave();
        }
        if (countryService.firstTime()) {
            countryService.harvestAndSave();
        }
        if (currencyService.firstTime()) {
            currencyService.harvestAndSave();
        }
        if (dataThemeService.firstTime()) {
            dataThemeService.harvestAndSave();
        }
        if (datasetTypeService.firstTime()) {
            datasetTypeService.harvestAndSave();
        }
        if (distributionStatusService.firstTime()) {
            distributionStatusService.harvestAndSave();
        }
        if (distributionTypeService.firstTime()) {
            distributionTypeService.harvestAndSave();
        }
        if (enhetService.firstTime()) {
            enhetService.harvestAndSave();
        }
        if (euroVocService.firstTime()) {
            euroVocService.harvestAndSave();
        }
        if (evidenceTypeService.firstTime()) {
            evidenceTypeService.harvestAndSave();
        }
        if (fileTypeService.firstTime()) {
            fileTypeService.harvestAndSave();
        }
        if (frequencyService.firstTime()) {
            frequencyService.harvestAndSave();
        }
        if (fylkeOrganisasjonService.firstTime()) {
            fylkeOrganisasjonService.harvestAndSave();
        }
        if (geonamesService.firstTime()) {
            geonamesService.harvestAndSave();
        }
        if (highValueCategoryService.firstTime()) {
            highValueCategoryService.harvestAndSave();
        }
        if (kommuneOrganisasjonService.firstTime()) {
            kommuneOrganisasjonService.harvestAndSave();
        }
        if (legalResourceTypeService.firstTime()) {
            legalResourceTypeService.harvestAndSave();
        }
        if (licenceService.firstTime()) {
            licenceService.harvestAndSave();
        }
        if (losService.firstTime()) {
            losService.importLosNodes();
        }
        if (mainActivityService.firstTime()) {
            mainActivityService.harvestAndSave();
        }
        if (mediaTypeService.firstTime()) {
            mediaTypeService.harvestAndSave();
        }
        if (mobilityConditionService.firstTime()) {
            mobilityConditionService.harvestAndSave();
        }
        if (mobilityDataStandardService.firstTime()) {
            mobilityDataStandardService.harvestAndSave();
        }
        if (mobilityThemeService.firstTime()) {
            mobilityThemeService.harvestAndSave();
        }
        if (plannedAvailabilityService.firstTime()) {
            plannedAvailabilityService.harvestAndSave();
        }
        if (qualityDimensionService.firstTime()) {
            qualityDimensionService.harvestAndSave();
        }
        if (relationshipWithSourceTypeService.firstTime()) {
            relationshipWithSourceTypeService.harvestAndSave();
        }
        if (roleTypeService.firstTime()) {
            roleTypeService.harvestAndSave();
        }
        if (serviceChannelTypeService.firstTime()) {
            serviceChannelTypeService.harvestAndSave();
        }

        if(languageService.firstTime()) {
            languageService.harvestAndSave();
        }
    }
}
