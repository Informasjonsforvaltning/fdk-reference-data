package no.fdk.referencedata;

import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubjectHarvester;
import no.fdk.referencedata.digdir.conceptsubjects.LocalConceptSubjectHarvester;
import no.fdk.referencedata.digdir.evidencetype.EvidenceTypeHarvester;
import no.fdk.referencedata.digdir.evidencetype.LocalEvidenceTypeHarvester;
import no.fdk.referencedata.digdir.legalresourcetype.LegalResourceTypeHarvester;
import no.fdk.referencedata.digdir.legalresourcetype.LocalLegalResourceTypeHarvester;
import no.fdk.referencedata.digdir.qualitydimension.LocalQualityDimensionHarvester;
import no.fdk.referencedata.digdir.qualitydimension.QualityDimensionHarvester;
import no.fdk.referencedata.digdir.roletype.LocalRoleTypeHarvester;
import no.fdk.referencedata.digdir.roletype.RoleTypeHarvester;
import no.fdk.referencedata.digdir.servicechanneltype.LocalServiceChannelTypeHarvester;
import no.fdk.referencedata.digdir.servicechanneltype.ServiceChannelTypeHarvester;
import no.fdk.referencedata.eu.accessright.AccessRightHarvester;
import no.fdk.referencedata.eu.accessright.LocalAccessRightHarvester;
import no.fdk.referencedata.eu.conceptstatus.ConceptStatusHarvester;
import no.fdk.referencedata.eu.conceptstatus.LocalConceptStatusHarvester;
import no.fdk.referencedata.eu.continent.ContinentHarvester;
import no.fdk.referencedata.eu.continent.LocalContinentHarvester;
import no.fdk.referencedata.eu.country.CountryHarvester;
import no.fdk.referencedata.eu.country.LocalCountryHarvester;
import no.fdk.referencedata.geonames.GeonamesHarvester;
import no.fdk.referencedata.geonames.LocalGeonamesHarvester;
import no.fdk.referencedata.eu.currency.CurrencyHarvester;
import no.fdk.referencedata.eu.currency.LocalCurrencyHarvester;
import no.fdk.referencedata.eu.datasettype.DatasetTypeHarvester;
import no.fdk.referencedata.eu.datasettype.LocalDatasetTypeHarvester;
import no.fdk.referencedata.eu.datatheme.DataThemeHarvester;
import no.fdk.referencedata.eu.datatheme.LocalDataThemeHarvester;
import no.fdk.referencedata.eu.distributionstatus.DistributionStatusHarvester;
import no.fdk.referencedata.eu.distributionstatus.LocalDistributionStatusHarvester;
import no.fdk.referencedata.eu.distributiontype.DistributionTypeHarvester;
import no.fdk.referencedata.eu.distributiontype.LocalDistributionTypeHarvester;
import no.fdk.referencedata.eu.eurovoc.EuroVocHarvester;
import no.fdk.referencedata.eu.eurovoc.LocalEuroVocHarvester;
import no.fdk.referencedata.eu.filetype.FileTypeHarvester;
import no.fdk.referencedata.eu.filetype.LocalFileTypeHarvester;
import no.fdk.referencedata.eu.frequency.FrequencyHarvester;
import no.fdk.referencedata.eu.frequency.LocalFrequencyHarvester;
import no.fdk.referencedata.eu.highvaluecategories.HighValueCategoriesHarvester;
import no.fdk.referencedata.eu.highvaluecategories.LocalHighValueCategoryHarvester;
import no.fdk.referencedata.eu.licence.LicenceHarvester;
import no.fdk.referencedata.eu.licence.LocalLicenceHarvester;
import no.fdk.referencedata.eu.mainactivity.LocalMainActivityHarvester;
import no.fdk.referencedata.eu.mainactivity.MainActivityHarvester;
import no.fdk.referencedata.eu.plannedavailability.LocalPlannedAvailabilityHarvester;
import no.fdk.referencedata.eu.plannedavailability.PlannedAvailabilityHarvester;
import no.fdk.referencedata.iana.mediatype.LocalMediaTypeHarvester;
import no.fdk.referencedata.iana.mediatype.MediaTypeHarvester;
import no.fdk.referencedata.los.LocalLosImporter;
import no.fdk.referencedata.los.LosImporter;
import no.fdk.referencedata.mobility.conditions.LocalMobilityConditionHarvester;
import no.fdk.referencedata.mobility.conditions.MobilityConditionHarvester;
import no.fdk.referencedata.mobility.datastandard.LocalMobilityDataStandardHarvester;
import no.fdk.referencedata.mobility.datastandard.MobilityDataStandardHarvester;
import no.fdk.referencedata.mobility.theme.LocalMobilityThemeHarvester;
import no.fdk.referencedata.mobility.theme.MobilityThemeHarvester;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class LocalHarvesterConfiguration {

    @Value("${wiremock.host:dummy}")
    private String wiremockHost;

    @Value("${wiremock.port:0}")
    private String wiremockPort;

    @Bean
    public MediaTypeHarvester mediaTypeHarvester() {
        return new LocalMediaTypeHarvester();
    }

    @Bean
    public FileTypeHarvester fileTypeHarvester() {
        return new LocalFileTypeHarvester();
    }

    @Bean
    public DataThemeHarvester dataThemeHarvester() {
        return new LocalDataThemeHarvester();
    }

    @Bean
    public EuroVocHarvester euroVocHarvester() {
        return new LocalEuroVocHarvester();
    }

    @Bean
    public AccessRightHarvester accessRightHarvester() {
        return new LocalAccessRightHarvester();
    }

    @Bean
    public FrequencyHarvester frequencyHarvester() {
        return new LocalFrequencyHarvester();
    }

    @Bean
    public ConceptStatusHarvester conceptStatusHarvester() {
        return new LocalConceptStatusHarvester();
    }

    @Bean
    public DistributionStatusHarvester distributionStatusHarvester() {
        return new LocalDistributionStatusHarvester();
    }

    @Bean
    public DistributionTypeHarvester distributionTypeHarvester() {
        return new LocalDistributionTypeHarvester();
    }

    @Bean
    public DatasetTypeHarvester datasetTypeHarvester() {
        return new LocalDatasetTypeHarvester();
    }

    @Bean
    public ConceptSubjectHarvester conceptSubjectHarvester(ApplicationSettings applicationSettings) {
        return new LocalConceptSubjectHarvester(applicationSettings);
    }

    @Bean
    public EvidenceTypeHarvester evidenceTypeHarvester() {
        return new LocalEvidenceTypeHarvester();
    }

    @Bean
    public MainActivityHarvester mainActivityHarvester() {
        return new LocalMainActivityHarvester();
    }

    @Bean
    public PlannedAvailabilityHarvester plannedAvailabilityHarvester() {
        return new LocalPlannedAvailabilityHarvester();
    }

    @Bean
    public RoleTypeHarvester roleTypeHarvester() {
        return new LocalRoleTypeHarvester();
    }

    @Bean
    public CurrencyHarvester currencyHarvester() {
        return new LocalCurrencyHarvester();
    }

    @Bean
    public LicenceHarvester licenceHarvester() {
        return new LocalLicenceHarvester();
    }

    @Bean
    public MobilityThemeHarvester mobilityThemeHarvester() {
        return new LocalMobilityThemeHarvester();
    }

    @Bean
    public MobilityDataStandardHarvester mobilityDataStandardHarvester() {
        return new LocalMobilityDataStandardHarvester();
    }

    @Bean
    public MobilityConditionHarvester mobilityConditionHarvester() {
        return new LocalMobilityConditionHarvester();
    }

    @Bean
    public LosImporter losImporter() {
        return new LocalLosImporter();
    }

    @Bean
    public ServiceChannelTypeHarvester serviceChannelTypeHarvester() {
        return new LocalServiceChannelTypeHarvester();
    }

    @Bean
    public HighValueCategoriesHarvester highValueCategoriesHarvester() {
        return new LocalHighValueCategoryHarvester();
    }

    @Bean
    public QualityDimensionHarvester qualityDimensionHarvester() {
        return new LocalQualityDimensionHarvester();
    }

    @Bean
    public LegalResourceTypeHarvester legalResourceTypeHarvester() {
        return new LocalLegalResourceTypeHarvester();
    }

    @Bean
    public CountryHarvester countryHarvester() {
        return new LocalCountryHarvester();
    }

    @Bean
    public ContinentHarvester continentHarvester() {
        return new LocalContinentHarvester();
    }

    @Bean
    public GeonamesHarvester geonamesHarvester() {
        return new LocalGeonamesHarvester(wiremockHost, wiremockPort);
    }
}
