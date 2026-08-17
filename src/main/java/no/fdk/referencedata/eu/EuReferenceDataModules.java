package no.fdk.referencedata.eu;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListApi;
import no.fdk.referencedata.core.CodeListApis;
import no.fdk.referencedata.core.CodeListRepository;
import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataModule;
import no.fdk.referencedata.eu.accessright.AccessRight;
import no.fdk.referencedata.eu.accessright.AccessRightRepository;
import no.fdk.referencedata.eu.accessright.AccessRightService;
import no.fdk.referencedata.eu.accessright.AccessRights;
import no.fdk.referencedata.eu.conceptstatus.ConceptStatus;
import no.fdk.referencedata.eu.conceptstatus.ConceptStatusRepository;
import no.fdk.referencedata.eu.conceptstatus.ConceptStatusService;
import no.fdk.referencedata.eu.conceptstatus.ConceptStatuses;
import no.fdk.referencedata.eu.continent.Continent;
import no.fdk.referencedata.eu.continent.ContinentRepository;
import no.fdk.referencedata.eu.continent.ContinentService;
import no.fdk.referencedata.eu.continent.Continents;
import no.fdk.referencedata.eu.country.Countries;
import no.fdk.referencedata.eu.country.Country;
import no.fdk.referencedata.eu.country.CountryRepository;
import no.fdk.referencedata.eu.country.CountryService;
import no.fdk.referencedata.eu.currency.Currencies;
import no.fdk.referencedata.eu.currency.Currency;
import no.fdk.referencedata.eu.currency.CurrencyRepository;
import no.fdk.referencedata.eu.currency.CurrencyService;
import no.fdk.referencedata.eu.datasettype.DatasetType;
import no.fdk.referencedata.eu.datasettype.DatasetTypeRepository;
import no.fdk.referencedata.eu.datasettype.DatasetTypeService;
import no.fdk.referencedata.eu.datasettype.DatasetTypes;
import no.fdk.referencedata.eu.datatheme.DataTheme;
import no.fdk.referencedata.eu.datatheme.DataThemeRepository;
import no.fdk.referencedata.eu.datatheme.DataThemeService;
import no.fdk.referencedata.eu.datatheme.DataThemes;
import no.fdk.referencedata.eu.distributionstatus.DistributionStatus;
import no.fdk.referencedata.eu.distributionstatus.DistributionStatusRepository;
import no.fdk.referencedata.eu.distributionstatus.DistributionStatusService;
import no.fdk.referencedata.eu.distributionstatus.DistributionStatuses;
import no.fdk.referencedata.eu.distributiontype.DistributionType;
import no.fdk.referencedata.eu.distributiontype.DistributionTypeRepository;
import no.fdk.referencedata.eu.distributiontype.DistributionTypeService;
import no.fdk.referencedata.eu.distributiontype.DistributionTypes;
import no.fdk.referencedata.eu.eurovoc.EuroVoc;
import no.fdk.referencedata.eu.eurovoc.EuroVocRepository;
import no.fdk.referencedata.eu.eurovoc.EuroVocService;
import no.fdk.referencedata.eu.eurovoc.EuroVocs;
import no.fdk.referencedata.eu.filetype.FileType;
import no.fdk.referencedata.eu.filetype.FileTypeRepository;
import no.fdk.referencedata.eu.filetype.FileTypeService;
import no.fdk.referencedata.eu.filetype.FileTypes;
import no.fdk.referencedata.eu.frequency.Frequencies;
import no.fdk.referencedata.eu.frequency.Frequency;
import no.fdk.referencedata.eu.frequency.FrequencyRepository;
import no.fdk.referencedata.eu.frequency.FrequencyService;
import no.fdk.referencedata.eu.highvaluecategories.HighValueCategories;
import no.fdk.referencedata.eu.highvaluecategories.HighValueCategory;
import no.fdk.referencedata.eu.highvaluecategories.HighValueCategoryRepository;
import no.fdk.referencedata.eu.highvaluecategories.HighValueCategoryService;
import no.fdk.referencedata.eu.language.Language;
import no.fdk.referencedata.eu.language.LanguageRepository;
import no.fdk.referencedata.eu.language.LanguageService;
import no.fdk.referencedata.eu.language.Languages;
import no.fdk.referencedata.eu.licence.Licence;
import no.fdk.referencedata.eu.licence.LicenceRepository;
import no.fdk.referencedata.eu.licence.LicenceService;
import no.fdk.referencedata.eu.licence.Licences;
import no.fdk.referencedata.eu.mainactivity.MainActivities;
import no.fdk.referencedata.eu.mainactivity.MainActivity;
import no.fdk.referencedata.eu.mainactivity.MainActivityRepository;
import no.fdk.referencedata.eu.mainactivity.MainActivityService;
import no.fdk.referencedata.eu.plannedavailability.PlannedAvailabilities;
import no.fdk.referencedata.eu.plannedavailability.PlannedAvailability;
import no.fdk.referencedata.eu.plannedavailability.PlannedAvailabilityRepository;
import no.fdk.referencedata.eu.plannedavailability.PlannedAvailabilityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;

import static no.fdk.referencedata.core.HarvestCron.*;

@Configuration
@RequiredArgsConstructor
public class EuReferenceDataModules {

    private final AccessRightService accessRightService;
    private final AccessRightRepository accessRightRepository;
    private final FileTypeService fileTypeService;
    private final FileTypeRepository fileTypeRepository;
    private final DataThemeService dataThemeService;
    private final DataThemeRepository dataThemeRepository;
    private final EuroVocService euroVocService;
    private final EuroVocRepository euroVocRepository;
    private final FrequencyService frequencyService;
    private final FrequencyRepository frequencyRepository;
    private final DistributionStatusService distributionStatusService;
    private final DistributionStatusRepository distributionStatusRepository;
    private final DistributionTypeService distributionTypeService;
    private final DistributionTypeRepository distributionTypeRepository;
    private final DatasetTypeService datasetTypeService;
    private final DatasetTypeRepository datasetTypeRepository;
    private final MainActivityService mainActivityService;
    private final MainActivityRepository mainActivityRepository;
    private final ConceptStatusService conceptStatusService;
    private final ConceptStatusRepository conceptStatusRepository;
    private final PlannedAvailabilityService plannedAvailabilityService;
    private final PlannedAvailabilityRepository plannedAvailabilityRepository;
    private final CurrencyService currencyService;
    private final CurrencyRepository currencyRepository;
    private final LicenceService licenceService;
    private final LicenceRepository licenceRepository;
    private final HighValueCategoryService highValueCategoryService;
    private final HighValueCategoryRepository highValueCategoryRepository;
    private final ContinentService continentService;
    private final ContinentRepository continentRepository;
    private final CountryService countryService;
    private final CountryRepository countryRepository;
    private final LanguageService languageService;
    private final LanguageRepository languageRepository;

    @Bean
    public ReferenceDataModule accessRightModule() {
        return module("access-right", accessRightService, accessRightApi(), CRON_ACCESS_RIGHT);
    }

    @Bean
    public CodeListApi<AccessRight> accessRightApi() {
        return CodeListApis.standard(
                "/eu/access-rights",
                CodeListRepository.of(accessRightRepository::findAll, accessRightRepository::findByCode),
                CodeListApis.sortByUri(AccessRight::getUri),
                list -> AccessRights.builder().accessRights(list).build(),
                accessRightService::getRdf,
                AccessRight.class);
    }

    @Bean
    public ReferenceDataModule fileTypeModule() {
        return module("file-type", fileTypeService, fileTypeApi(), CRON_FILE_TYPE);
    }

    @Bean
    public CodeListApi<FileType> fileTypeApi() {
        return CodeListApis.standard(
                "/eu/file-types",
                CodeListRepository.of(fileTypeRepository::findAll, fileTypeRepository::findByCode),
                CodeListApis.sortByUri(FileType::getUri),
                list -> FileTypes.builder().fileTypes(list).build(),
                fileTypeService::getRdf,
                FileType.class);
    }

    @Bean
    public ReferenceDataModule dataThemeModule() {
        return module("data-theme", dataThemeService, dataThemeApi(), CRON_DATA_THEME);
    }

    @Bean
    public CodeListApi<DataTheme> dataThemeApi() {
        return CodeListApis.standard(
                "/eu/data-themes",
                CodeListRepository.of(dataThemeRepository::findAll, dataThemeRepository::findByCode),
                CodeListApis.sortByUri(DataTheme::getUri),
                list -> DataThemes.builder().dataThemes(list).build(),
                dataThemeService::getRdf,
                DataTheme.class);
    }

    @Bean
    public ReferenceDataModule euroVocModule() {
        return module("eurovoc", euroVocService, euroVocApi(), CRON_EUROVOC);
    }

    @Bean
    public CodeListApi<EuroVoc> euroVocApi() {
        return CodeListApis.standard(
                "/eu/eurovocs",
                CodeListRepository.of(euroVocRepository::findAll, euroVocRepository::findByCode),
                CodeListApis.sortByUri(EuroVoc::getUri),
                list -> EuroVocs.builder().euroVocs(list).build(),
                euroVocService::getRdf,
                EuroVoc.class);
    }

    @Bean
    public ReferenceDataModule frequencyModule() {
        return module("frequency", frequencyService, frequencyApi(), CRON_FREQUENCY);
    }

    @Bean
    public CodeListApi<Frequency> frequencyApi() {
        return CodeListApis.standard(
                "/eu/frequencies",
                CodeListRepository.of(frequencyRepository::findAll, frequencyRepository::findByCode),
                Comparator.comparing(Frequency::getSortIndex),
                list -> Frequencies.builder().frequencies(list).build(),
                frequencyService::getRdf,
                Frequency.class);
    }

    @Bean
    public ReferenceDataModule distributionStatusModule() {
        return module("distribution-status", distributionStatusService, distributionStatusApi(), CRON_DISTRIBUTION_STATUS);
    }

    @Bean
    public CodeListApi<DistributionStatus> distributionStatusApi() {
        return CodeListApis.standard(
                "/eu/distribution-statuses",
                CodeListRepository.of(distributionStatusRepository::findAll, distributionStatusRepository::findByCode),
                CodeListApis.sortByUri(DistributionStatus::getUri),
                list -> DistributionStatuses.builder().distributionStatuses(list).build(),
                distributionStatusService::getRdf,
                DistributionStatus.class);
    }

    @Bean
    public ReferenceDataModule distributionTypeModule() {
        return module("distribution-type", distributionTypeService, distributionTypeApi(), CRON_DISTRIBUTION_TYPE);
    }

    @Bean
    public CodeListApi<DistributionType> distributionTypeApi() {
        return CodeListApis.standard(
                "/eu/distribution-types",
                CodeListRepository.of(distributionTypeRepository::findAll, distributionTypeRepository::findByCode),
                CodeListApis.sortByUri(DistributionType::getUri),
                list -> DistributionTypes.builder().distributionTypes(list).build(),
                distributionTypeService::getRdf,
                DistributionType.class);
    }

    @Bean
    public ReferenceDataModule datasetTypeModule() {
        return module("dataset-type", datasetTypeService, datasetTypeApi(), CRON_DATASET_TYPE);
    }

    @Bean
    public CodeListApi<DatasetType> datasetTypeApi() {
        return CodeListApis.standard(
                "/eu/dataset-types",
                CodeListRepository.of(datasetTypeRepository::findAll, datasetTypeRepository::findByCode),
                CodeListApis.sortByUri(DatasetType::getUri),
                list -> DatasetTypes.builder().datasetTypes(list).build(),
                datasetTypeService::getRdf,
                DatasetType.class);
    }

    @Bean
    public ReferenceDataModule mainActivityModule() {
        return module("main-activity", mainActivityService, mainActivityApi(), CRON_MAIN_ACTIVITY);
    }

    @Bean
    public CodeListApi<MainActivity> mainActivityApi() {
        return CodeListApis.standard(
                "/eu/main-activities",
                CodeListRepository.of(mainActivityRepository::findAll, mainActivityRepository::findByCode),
                CodeListApis.sortByUri(MainActivity::getUri),
                list -> MainActivities.builder().mainActivities(list).build(),
                mainActivityService::getRdf,
                MainActivity.class);
    }

    @Bean
    public ReferenceDataModule conceptStatusModule() {
        return module("concept-status", conceptStatusService, conceptStatusApi(), CRON_CONCEPT_STATUS);
    }

    @Bean
    public CodeListApi<ConceptStatus> conceptStatusApi() {
        return CodeListApis.standard(
                "/eu/concept-statuses",
                CodeListRepository.of(conceptStatusRepository::findAll, conceptStatusRepository::findByCode),
                CodeListApis.sortByUri(ConceptStatus::getUri),
                list -> ConceptStatuses.builder().conceptStatuses(list).build(),
                conceptStatusService::getRdf,
                ConceptStatus.class);
    }

    @Bean
    public ReferenceDataModule plannedAvailabilityModule() {
        return module("planned-availability", plannedAvailabilityService, plannedAvailabilityApi(), CRON_PLANNED_AVAILABILITY);
    }

    @Bean
    public CodeListApi<PlannedAvailability> plannedAvailabilityApi() {
        return CodeListApis.standard(
                "/eu/planned-availabilities",
                CodeListRepository.of(plannedAvailabilityRepository::findAll, plannedAvailabilityRepository::findByCode),
                CodeListApis.sortByUri(PlannedAvailability::getUri),
                list -> PlannedAvailabilities.builder().plannedAvailabilities(list).build(),
                plannedAvailabilityService::getRdf,
                PlannedAvailability.class);
    }

    @Bean
    public ReferenceDataModule currencyModule() {
        return module("currency", currencyService, currencyApi(), CRON_CURRENCY);
    }

    @Bean
    public CodeListApi<Currency> currencyApi() {
        return CodeListApis.standard(
                "/eu/currencies",
                CodeListRepository.of(currencyRepository::findAll, currencyRepository::findByCode),
                CodeListApis.sortByUri(Currency::getUri),
                list -> Currencies.builder().currencies(list).build(),
                currencyService::getRdf,
                Currency.class);
    }

    @Bean
    public ReferenceDataModule licenceModule() {
        return module("licence", licenceService, licenceApi(), CRON_LICENCE);
    }

    @Bean
    public CodeListApi<Licence> licenceApi() {
        return CodeListApis.standard(
                "/eu/licences",
                CodeListRepository.of(licenceRepository::findAll, licenceRepository::findByCode),
                CodeListApis.sortByUri(Licence::getUri),
                list -> Licences.builder().licences(list).build(),
                licenceService::getRdf,
                Licence.class);
    }

    @Bean
    public ReferenceDataModule highValueCategoryModule() {
        return module("high-value-category", highValueCategoryService, highValueCategoryApi(), CRON_HIGH_VALUE_CATEGORY);
    }

    @Bean
    public CodeListApi<HighValueCategory> highValueCategoryApi() {
        return CodeListApis.standard(
                "/eu/high-value-categories",
                CodeListRepository.of(highValueCategoryRepository::findAll, highValueCategoryRepository::findByCode),
                CodeListApis.sortByUri(HighValueCategory::getUri),
                list -> HighValueCategories.builder().highValueCategories(list).build(),
                highValueCategoryService::getRdf,
                HighValueCategory.class);
    }

    @Bean
    public ReferenceDataModule continentModule() {
        return module("continent", continentService, continentApi(), CRON_CONTINENT);
    }

    @Bean
    public CodeListApi<Continent> continentApi() {
        return CodeListApis.standard(
                "/eu/continents",
                CodeListRepository.of(continentRepository::findAll, continentRepository::findByCode),
                CodeListApis.sortByUri(Continent::getUri),
                list -> Continents.builder().continents(list).build(),
                continentService::getRdf,
                Continent.class);
    }

    @Bean
    public ReferenceDataModule countryModule() {
        return module("country", countryService, countryApi(), CRON_COUNTRY);
    }

    @Bean
    public CodeListApi<Country> countryApi() {
        return CodeListApis.standard(
                "/eu/countries",
                CodeListRepository.of(countryRepository::findAll, countryRepository::findByCode),
                CodeListApis.sortByUri(Country::getUri),
                list -> Countries.builder().countries(list).build(),
                countryService::getRdf,
                Country.class);
    }

    @Bean
    public ReferenceDataModule languageModule() {
        return module("language", languageService, languageApi(), CRON_LANGUAGE);
    }

    @Bean
    public CodeListApi<Language> languageApi() {
        return CodeListApis.standard(
                "/eu/languages",
                CodeListRepository.of(languageRepository::findAll, languageRepository::findByCode),
                CodeListApis.sortByUri(Language::getUri),
                list -> Languages.builder().languages(list).build(),
                languageService::getRdf,
                Language.class);
    }

    private static ReferenceDataModule module(String id, HarvestableReferenceData service, CodeListApi<?> api, String cron) {
        return new ReferenceDataModule(id, service, api, cron);
    }
}
