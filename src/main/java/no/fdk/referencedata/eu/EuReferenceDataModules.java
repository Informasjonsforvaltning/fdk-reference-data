package no.fdk.referencedata.eu;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.ReferenceDataModule;
import no.fdk.referencedata.core.ScheduleSpec;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
public class EuReferenceDataModules {

    static final String CRON_ACCESS_RIGHT = "0 30 1 1 * ?";
    static final String CRON_FILE_TYPE = "0 50 1 1 * ?";
    static final String CRON_DATA_THEME = "0 0 2 1 * ?";
    static final String CRON_EUROVOC = "0 10 2 1 * ?";
    static final String CRON_FREQUENCY = "0 20 2 1 * ?";
    static final String CRON_DISTRIBUTION_STATUS = "0 25 2 1 * ?";
    static final String CRON_DISTRIBUTION_TYPE = "0 30 2 1 * ?";
    static final String CRON_DATASET_TYPE = "0 35 2 1 * ?";
    static final String CRON_MAIN_ACTIVITY = "0 40 2 1 * ?";
    static final String CRON_CONCEPT_STATUS = "0 40 3 1 * ?";
    static final String CRON_PLANNED_AVAILABILITY = "0 20 4 1 * ?";
    static final String CRON_CURRENCY = "0 30 4 1 * ?";
    static final String CRON_LICENCE = "0 40 4 1 * ?";
    static final String CRON_HIGH_VALUE_CATEGORY = "0 5 5 1 * ?";
    static final String CRON_CONTINENT = "0 20 5 1 * ?";
    static final String CRON_COUNTRY = "0 25 5 1 * ?";
    static final String CRON_LANGUAGE = "0 35 5 1 * ?";

    private final AccessRightService accessRightService;
    private final FileTypeService fileTypeService;
    private final DataThemeService dataThemeService;
    private final EuroVocService euroVocService;
    private final FrequencyService frequencyService;
    private final DistributionStatusService distributionStatusService;
    private final DistributionTypeService distributionTypeService;
    private final DatasetTypeService datasetTypeService;
    private final MainActivityService mainActivityService;
    private final ConceptStatusService conceptStatusService;
    private final PlannedAvailabilityService plannedAvailabilityService;
    private final CurrencyService currencyService;
    private final LicenceService licenceService;
    private final HighValueCategoryService highValueCategoryService;
    private final ContinentService continentService;
    private final CountryService countryService;
    private final LanguageService languageService;

    @Bean
    public ReferenceDataModule accessRightModule() {
        return new ReferenceDataModule("access-right", ScheduleSpec.of(CRON_ACCESS_RIGHT), accessRightService);
    }

    @Scheduled(cron = CRON_ACCESS_RIGHT)
    public void updateAccessRights() {
        accessRightService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule fileTypeModule() {
        return new ReferenceDataModule("file-type", ScheduleSpec.of(CRON_FILE_TYPE), fileTypeService);
    }

    @Scheduled(cron = CRON_FILE_TYPE)
    public void updateFileTypes() {
        fileTypeService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule dataThemeModule() {
        return new ReferenceDataModule("data-theme", ScheduleSpec.of(CRON_DATA_THEME), dataThemeService);
    }

    @Scheduled(cron = CRON_DATA_THEME)
    public void updateDataThemes() {
        dataThemeService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule euroVocModule() {
        return new ReferenceDataModule("eurovoc", ScheduleSpec.of(CRON_EUROVOC), euroVocService);
    }

    @Scheduled(cron = CRON_EUROVOC)
    public void updateEuroVoc() {
        euroVocService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule frequencyModule() {
        return new ReferenceDataModule("frequency", ScheduleSpec.of(CRON_FREQUENCY), frequencyService);
    }

    @Scheduled(cron = CRON_FREQUENCY)
    public void updateFrequencies() {
        frequencyService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule distributionStatusModule() {
        return new ReferenceDataModule("distribution-status", ScheduleSpec.of(CRON_DISTRIBUTION_STATUS), distributionStatusService);
    }

    @Scheduled(cron = CRON_DISTRIBUTION_STATUS)
    public void updateDistributionStatuses() {
        distributionStatusService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule distributionTypeModule() {
        return new ReferenceDataModule("distribution-type", ScheduleSpec.of(CRON_DISTRIBUTION_TYPE), distributionTypeService);
    }

    @Scheduled(cron = CRON_DISTRIBUTION_TYPE)
    public void updateDistributionTypes() {
        distributionTypeService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule datasetTypeModule() {
        return new ReferenceDataModule("dataset-type", ScheduleSpec.of(CRON_DATASET_TYPE), datasetTypeService);
    }

    @Scheduled(cron = CRON_DATASET_TYPE)
    public void updateDatasetTypes() {
        datasetTypeService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule mainActivityModule() {
        return new ReferenceDataModule("main-activity", ScheduleSpec.of(CRON_MAIN_ACTIVITY), mainActivityService);
    }

    @Scheduled(cron = CRON_MAIN_ACTIVITY)
    public void updateMainActivities() {
        mainActivityService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule conceptStatusModule() {
        return new ReferenceDataModule("concept-status", ScheduleSpec.of(CRON_CONCEPT_STATUS), conceptStatusService);
    }

    @Scheduled(cron = CRON_CONCEPT_STATUS)
    public void updateConceptStatuses() {
        conceptStatusService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule plannedAvailabilityModule() {
        return new ReferenceDataModule("planned-availability", ScheduleSpec.of(CRON_PLANNED_AVAILABILITY), plannedAvailabilityService);
    }

    @Scheduled(cron = CRON_PLANNED_AVAILABILITY)
    public void updatePlannedAvailability() {
        plannedAvailabilityService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule currencyModule() {
        return new ReferenceDataModule("currency", ScheduleSpec.of(CRON_CURRENCY), currencyService);
    }

    @Scheduled(cron = CRON_CURRENCY)
    public void updateCurrencies() {
        currencyService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule licenceModule() {
        return new ReferenceDataModule("licence", ScheduleSpec.of(CRON_LICENCE), licenceService);
    }

    @Scheduled(cron = CRON_LICENCE)
    public void updateLicences() {
        licenceService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule highValueCategoryModule() {
        return new ReferenceDataModule("high-value-category", ScheduleSpec.of(CRON_HIGH_VALUE_CATEGORY), highValueCategoryService);
    }

    @Scheduled(cron = CRON_HIGH_VALUE_CATEGORY)
    public void updateHighValueCategories() {
        highValueCategoryService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule continentModule() {
        return new ReferenceDataModule("continent", ScheduleSpec.of(CRON_CONTINENT), continentService);
    }

    @Scheduled(cron = CRON_CONTINENT)
    public void updateContinents() {
        continentService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule countryModule() {
        return new ReferenceDataModule("country", ScheduleSpec.of(CRON_COUNTRY), countryService);
    }

    @Scheduled(cron = CRON_COUNTRY)
    public void updateCountries() {
        countryService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule languageModule() {
        return new ReferenceDataModule("language", ScheduleSpec.of(CRON_LANGUAGE), languageService);
    }

    @Scheduled(cron = CRON_LANGUAGE)
    public void updateLanguages() {
        languageService.harvestAndSave();
    }
}
