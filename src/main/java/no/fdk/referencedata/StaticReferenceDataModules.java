package no.fdk.referencedata;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.adms.publishertype.PublisherType;
import no.fdk.referencedata.adms.publishertype.PublisherTypeRepository;
import no.fdk.referencedata.adms.publishertype.PublisherTypeService;
import no.fdk.referencedata.adms.publishertype.PublisherTypes;
import no.fdk.referencedata.adms.status.ADMSStatus;
import no.fdk.referencedata.adms.status.ADMSStatusRepository;
import no.fdk.referencedata.adms.status.ADMSStatusService;
import no.fdk.referencedata.adms.status.ADMSStatuses;
import no.fdk.referencedata.apispecification.ApiSpecification;
import no.fdk.referencedata.apispecification.ApiSpecificationService;
import no.fdk.referencedata.apispecification.ApiSpecifications;
import no.fdk.referencedata.apistatus.ApiStatus;
import no.fdk.referencedata.apistatus.ApiStatusService;
import no.fdk.referencedata.apistatus.ApiStatuses;
import no.fdk.referencedata.core.CodeListApi;
import no.fdk.referencedata.core.CodeListApis;
import no.fdk.referencedata.core.CodeListRepository;
import no.fdk.referencedata.core.ReferenceDataModule;
import no.fdk.referencedata.linguisticsystem.LinguisticSystem;
import no.fdk.referencedata.linguisticsystem.LinguisticSystemService;
import no.fdk.referencedata.linguisticsystem.LinguisticSystems;
import no.fdk.referencedata.openlicences.OpenLicense;
import no.fdk.referencedata.openlicences.OpenLicenseService;
import no.fdk.referencedata.openlicences.OpenLicenses;
import no.fdk.referencedata.provenancestatement.ProvenanceStatement;
import no.fdk.referencedata.provenancestatement.ProvenanceStatementService;
import no.fdk.referencedata.provenancestatement.ProvenanceStatements;
import no.fdk.referencedata.referencetypes.ReferenceType;
import no.fdk.referencedata.referencetypes.ReferenceTypeService;
import no.fdk.referencedata.referencetypes.ReferenceTypes;
import no.fdk.referencedata.schema.dayofweek.DayOfWeek;
import no.fdk.referencedata.schema.dayofweek.DayOfWeekService;
import no.fdk.referencedata.schema.dayofweek.WeekDays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class StaticReferenceDataModules {

    private final ADMSStatusService admsStatusService;
    private final ADMSStatusRepository admsStatusRepository;
    private final PublisherTypeService publisherTypeService;
    private final PublisherTypeRepository publisherTypeRepository;
    private final ApiStatusService apiStatusService;
    private final ApiSpecificationService apiSpecificationService;
    private final OpenLicenseService openLicenseService;
    private final LinguisticSystemService linguisticSystemService;
    private final ProvenanceStatementService provenanceStatementService;
    private final DayOfWeekService dayOfWeekService;
    private final ReferenceTypeService referenceTypeService;

    @Bean
    public ReferenceDataModule admsStatusModule() {
        return new ReferenceDataModule("adms-status", admsStatusApi());
    }

    @Bean
    public CodeListApi<ADMSStatus> admsStatusApi() {
        return CodeListApis.readOnly(
                "/adms/statuses",
                CodeListRepository.of(admsStatusRepository::findAll, admsStatusRepository::findByCode),
                null,
                list -> ADMSStatuses.builder().statuses(list).build(),
                admsStatusService::getRdf,
                ADMSStatus.class);
    }

    @Bean
    public ReferenceDataModule publisherTypeModule() {
        return new ReferenceDataModule("adms-publisher-type", publisherTypeApi());
    }

    @Bean
    public CodeListApi<PublisherType> publisherTypeApi() {
        return CodeListApis.readOnly(
                "/adms/publisher-types",
                CodeListRepository.of(publisherTypeRepository::findAll, publisherTypeRepository::findByCode),
                null,
                list -> PublisherTypes.builder().publisherTypes(list).build(),
                publisherTypeService::getRdf,
                PublisherType.class);
    }

    @Bean
    public ReferenceDataModule apiStatusModule() {
        return new ReferenceDataModule("api-status", apiStatusApi());
    }

    @Bean
    public CodeListApi<ApiStatus> apiStatusApi() {
        return CodeListApis.readOnly(
                "/api-status",
                CodeListRepository.of(apiStatusService::getAll, apiStatusService::getByCode),
                null,
                list -> ApiStatuses.builder().apiStatuses(list).build(),
                apiStatusService::getRdf,
                ApiStatus.class);
    }

    @Bean
    public ReferenceDataModule apiSpecificationModule() {
        return new ReferenceDataModule("api-specification", apiSpecificationApi());
    }

    @Bean
    public CodeListApi<ApiSpecification> apiSpecificationApi() {
        return CodeListApis.readOnly(
                "/api-specifications",
                CodeListRepository.of(apiSpecificationService::getAll, apiSpecificationService::getByCode),
                null,
                list -> ApiSpecifications.builder().apiSpecifications(list).build(),
                apiSpecificationService::getRdf,
                ApiSpecification.class);
    }

    @Bean
    public ReferenceDataModule openLicenseModule() {
        return new ReferenceDataModule("open-license", openLicenseApi());
    }

    @Bean
    public CodeListApi<OpenLicense> openLicenseApi() {
        return CodeListApis.readOnly(
                "/open-licenses",
                CodeListRepository.of(openLicenseService::getAll, openLicenseService::getByCode),
                null,
                list -> OpenLicenses.builder().openLicenses(list).build(),
                openLicenseService::getRdf,
                OpenLicense.class);
    }

    @Bean
    public ReferenceDataModule linguisticSystemModule() {
        return new ReferenceDataModule("linguistic-system", linguisticSystemApi());
    }

    @Bean
    public CodeListApi<LinguisticSystem> linguisticSystemApi() {
        return CodeListApis.readOnly(
                "/linguistic-systems",
                CodeListRepository.of(linguisticSystemService::getAll, linguisticSystemService::getByCode),
                null,
                list -> LinguisticSystems.builder().linguisticSystems(list).build(),
                linguisticSystemService::getRdf,
                LinguisticSystem.class);
    }

    @Bean
    public ReferenceDataModule provenanceStatementModule() {
        return new ReferenceDataModule("provenance-statement", provenanceStatementApi());
    }

    @Bean
    public CodeListApi<ProvenanceStatement> provenanceStatementApi() {
        return CodeListApis.readOnly(
                "/provenance-statements",
                CodeListRepository.of(provenanceStatementService::getAll, provenanceStatementService::getByCode),
                null,
                list -> ProvenanceStatements.builder().provenanceStatements(list).build(),
                provenanceStatementService::getRdf,
                ProvenanceStatement.class);
    }

    @Bean
    public ReferenceDataModule dayOfWeekModule() {
        return new ReferenceDataModule("day-of-week", dayOfWeekApi());
    }

    @Bean
    public CodeListApi<DayOfWeek> dayOfWeekApi() {
        return CodeListApis.readOnly(
                "/schema/week-days",
                CodeListRepository.of(dayOfWeekService::getAll, dayOfWeekService::getByCode),
                null,
                list -> WeekDays.builder().weekDays(list).build(),
                dayOfWeekService::getRdf,
                DayOfWeek.class);
    }

    @Bean
    public ReferenceDataModule referenceTypeModule() {
        return new ReferenceDataModule("reference-type", referenceTypeApi());
    }

    @Bean
    public CodeListApi<ReferenceType> referenceTypeApi() {
        return CodeListApis.readOnly(
                "/reference-types",
                CodeListRepository.of(referenceTypeService::getAll, referenceTypeService::getByCode),
                null,
                list -> ReferenceTypes.builder().referenceTypes(list).build(),
                null,
                ReferenceType.class);
    }
}
