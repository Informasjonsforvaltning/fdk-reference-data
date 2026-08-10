package no.fdk.referencedata;

import no.fdk.referencedata.digdir.audiencetype.AudienceTypeHarvester;
import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubjectHarvester;
import no.fdk.referencedata.digdir.evidencetype.EvidenceTypeHarvester;
import no.fdk.referencedata.digdir.legalresourcetype.LegalResourceTypeHarvester;
import no.fdk.referencedata.digdir.qualitydimension.QualityDimensionHarvester;
import no.fdk.referencedata.digdir.relationshipwithsourcetype.RelationshipWithSourceTypeHarvester;
import no.fdk.referencedata.digdir.roletype.RoleTypeHarvester;
import no.fdk.referencedata.digdir.servicechanneltype.ServiceChannelTypeHarvester;
import no.fdk.referencedata.eu.accessright.AccessRightHarvester;
import no.fdk.referencedata.eu.conceptstatus.ConceptStatusHarvester;
import no.fdk.referencedata.eu.continent.ContinentHarvester;
import no.fdk.referencedata.eu.country.CountryHarvester;
import no.fdk.referencedata.eu.currency.CurrencyHarvester;
import no.fdk.referencedata.eu.datasettype.DatasetTypeHarvester;
import no.fdk.referencedata.eu.datatheme.DataThemeHarvester;
import no.fdk.referencedata.eu.distributionstatus.DistributionStatusHarvester;
import no.fdk.referencedata.eu.distributiontype.DistributionTypeHarvester;
import no.fdk.referencedata.eu.eurovoc.EuroVocHarvester;
import no.fdk.referencedata.eu.filetype.FileTypeHarvester;
import no.fdk.referencedata.eu.frequency.FrequencyHarvester;
import no.fdk.referencedata.eu.highvaluecategories.HighValueCategoriesHarvester;
import no.fdk.referencedata.eu.language.LanguageHarvester;
import no.fdk.referencedata.eu.licence.LicenceHarvester;
import no.fdk.referencedata.eu.mainactivity.MainActivityHarvester;
import no.fdk.referencedata.eu.plannedavailability.PlannedAvailabilityHarvester;
import no.fdk.referencedata.geonames.GeonamesHarvester;
import no.fdk.referencedata.geonames.LocalGeonamesHarvester;
import no.fdk.referencedata.geonorge.administrativeenheter.EnhetHarvester;
import no.fdk.referencedata.geonorge.administrativeenheter.LocalEnhetHarvester;
import no.fdk.referencedata.iana.mediatype.LocalMediaTypeHarvester;
import no.fdk.referencedata.iana.mediatype.MediaTypeHarvester;
import no.fdk.referencedata.los.LocalLosImporter;
import no.fdk.referencedata.los.LosImporter;
import no.fdk.referencedata.mobility.conditions.MobilityConditionHarvester;
import no.fdk.referencedata.mobility.datastandard.MobilityDataStandardHarvester;
import no.fdk.referencedata.mobility.theme.MobilityThemeHarvester;
import no.fdk.referencedata.ssb.fylkeorganisasjoner.FylkeOrganisasjonHarvester;
import no.fdk.referencedata.ssb.fylkeorganisasjoner.LocalFylkeOrganisasjonHarvester;
import no.fdk.referencedata.ssb.kommuneorganisasjoner.KommuneOrganisasjonHarvester;
import no.fdk.referencedata.ssb.kommuneorganisasjoner.LocalKommuneOrganisasjonHarvester;
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
        return LocalHarvesters.fileType();
    }

    @Bean
    public DataThemeHarvester dataThemeHarvester() {
        return LocalHarvesters.dataTheme();
    }

    @Bean
    public EuroVocHarvester euroVocHarvester() {
        return LocalHarvesters.euroVoc();
    }

    @Bean
    public AccessRightHarvester accessRightHarvester() {
        return LocalHarvesters.accessRight();
    }

    @Bean
    public FrequencyHarvester frequencyHarvester() {
        return LocalHarvesters.frequency();
    }

    @Bean
    public ConceptStatusHarvester conceptStatusHarvester() {
        return LocalHarvesters.conceptStatus();
    }

    @Bean
    public DistributionStatusHarvester distributionStatusHarvester() {
        return LocalHarvesters.distributionStatus();
    }

    @Bean
    public DistributionTypeHarvester distributionTypeHarvester() {
        return LocalHarvesters.distributionType();
    }

    @Bean
    public DatasetTypeHarvester datasetTypeHarvester() {
        return LocalHarvesters.datasetType();
    }

    @Bean
    public ConceptSubjectHarvester conceptSubjectHarvester(ApplicationSettings applicationSettings) {
        return LocalHarvesters.conceptSubject(applicationSettings);
    }

    @Bean
    public EvidenceTypeHarvester evidenceTypeHarvester() {
        return LocalHarvesters.evidenceType();
    }

    @Bean
    public MainActivityHarvester mainActivityHarvester() {
        return LocalHarvesters.mainActivity();
    }

    @Bean
    public PlannedAvailabilityHarvester plannedAvailabilityHarvester() {
        return LocalHarvesters.plannedAvailability();
    }

    @Bean
    public AudienceTypeHarvester audienceTypeHarvester() {
        return LocalHarvesters.audienceType();
    }

    @Bean
    public RoleTypeHarvester roleTypeHarvester() {
        return LocalHarvesters.roleType();
    }

    @Bean
    public RelationshipWithSourceTypeHarvester relationshipWithSourceTypeHarvester() {
        return LocalHarvesters.relationshipWithSourceType();
    }

    @Bean
    public CurrencyHarvester currencyHarvester() {
        return LocalHarvesters.currency();
    }

    @Bean
    public LanguageHarvester languageHarvester() {
        return LocalHarvesters.language();
    }

    @Bean
    public LicenceHarvester licenceHarvester() {
        return LocalHarvesters.licence();
    }

    @Bean
    public MobilityThemeHarvester mobilityThemeHarvester() {
        return LocalHarvesters.mobilityTheme();
    }

    @Bean
    public MobilityDataStandardHarvester mobilityDataStandardHarvester() {
        return LocalHarvesters.mobilityDataStandard();
    }

    @Bean
    public MobilityConditionHarvester mobilityConditionHarvester() {
        return LocalHarvesters.mobilityCondition();
    }

    @Bean
    public LosImporter losImporter() {
        return new LocalLosImporter();
    }

    @Bean
    public ServiceChannelTypeHarvester serviceChannelTypeHarvester() {
        return LocalHarvesters.serviceChannelType();
    }

    @Bean
    public HighValueCategoriesHarvester highValueCategoriesHarvester() {
        return LocalHarvesters.highValueCategory();
    }

    @Bean
    public QualityDimensionHarvester qualityDimensionHarvester() {
        return LocalHarvesters.qualityDimension();
    }

    @Bean
    public LegalResourceTypeHarvester legalResourceTypeHarvester() {
        return LocalHarvesters.legalResourceType();
    }

    @Bean
    public CountryHarvester countryHarvester() {
        return LocalHarvesters.country();
    }

    @Bean
    public ContinentHarvester continentHarvester() {
        return LocalHarvesters.continent();
    }

    @Bean
    public GeonamesHarvester geonamesHarvester() {
        return new LocalGeonamesHarvester(wiremockHost, wiremockPort);
    }

    @Bean
    public EnhetHarvester enhetHarvester() {
        return new LocalEnhetHarvester();
    }

    @Bean
    public FylkeOrganisasjonHarvester fylkeOrganisasjonHarvester() {
        return new LocalFylkeOrganisasjonHarvester(wiremockHost, wiremockPort);
    }

    @Bean
    public KommuneOrganisasjonHarvester kommuneOrganisasjonHarvester() {
        return new LocalKommuneOrganisasjonHarvester(wiremockHost, wiremockPort);
    }
}
