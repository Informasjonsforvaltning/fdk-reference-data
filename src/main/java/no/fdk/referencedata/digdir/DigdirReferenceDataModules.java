package no.fdk.referencedata.digdir;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListApi;
import no.fdk.referencedata.core.CodeListApis;
import no.fdk.referencedata.core.CodeListRepository;
import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataModule;
import no.fdk.referencedata.digdir.audiencetype.AudienceType;
import no.fdk.referencedata.digdir.audiencetype.AudienceTypeRepository;
import no.fdk.referencedata.digdir.audiencetype.AudienceTypeService;
import no.fdk.referencedata.digdir.audiencetype.AudienceTypes;
import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubject;
import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubjectRepository;
import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubjectService;
import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubjects;
import no.fdk.referencedata.digdir.evidencetype.EvidenceType;
import no.fdk.referencedata.digdir.evidencetype.EvidenceTypeRepository;
import no.fdk.referencedata.digdir.evidencetype.EvidenceTypeService;
import no.fdk.referencedata.digdir.evidencetype.EvidenceTypes;
import no.fdk.referencedata.digdir.legalresourcetype.LegalResourceType;
import no.fdk.referencedata.digdir.legalresourcetype.LegalResourceTypeRepository;
import no.fdk.referencedata.digdir.legalresourcetype.LegalResourceTypeService;
import no.fdk.referencedata.digdir.legalresourcetype.LegalResourceTypes;
import no.fdk.referencedata.digdir.qualitydimension.QualityDimension;
import no.fdk.referencedata.digdir.qualitydimension.QualityDimensionRepository;
import no.fdk.referencedata.digdir.qualitydimension.QualityDimensionService;
import no.fdk.referencedata.digdir.qualitydimension.QualityDimensions;
import no.fdk.referencedata.digdir.relationshipwithsourcetype.RelationshipWithSourceType;
import no.fdk.referencedata.digdir.relationshipwithsourcetype.RelationshipWithSourceTypeRepository;
import no.fdk.referencedata.digdir.relationshipwithsourcetype.RelationshipWithSourceTypeService;
import no.fdk.referencedata.digdir.relationshipwithsourcetype.RelationshipWithSourceTypes;
import no.fdk.referencedata.digdir.roletype.RoleType;
import no.fdk.referencedata.digdir.roletype.RoleTypeRepository;
import no.fdk.referencedata.digdir.roletype.RoleTypeService;
import no.fdk.referencedata.digdir.roletype.RoleTypes;
import no.fdk.referencedata.digdir.servicechanneltype.ServiceChannelType;
import no.fdk.referencedata.digdir.servicechanneltype.ServiceChannelTypeRepository;
import no.fdk.referencedata.digdir.servicechanneltype.ServiceChannelTypeService;
import no.fdk.referencedata.digdir.servicechanneltype.ServiceChannelTypes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
public class DigdirReferenceDataModules {

    static final String CRON_CONCEPT_SUBJECT = "0 45 * * * ?";
    static final String CRON_EVIDENCE_TYPE = "0 0 1 1 * ?";
    static final String CRON_SERVICE_CHANNEL_TYPE = "0 10 1 1 * ?";
    static final String CRON_ROLE_TYPE = "0 20 1 1 * ?";
    static final String CRON_AUDIENCE_TYPE = "0 50 3 1 * ?";
    static final String CRON_RELATIONSHIP_WITH_SOURCE_TYPE = "0 10 4 1 * ?";
    static final String CRON_QUALITY_DIMENSION = "0 10 5 1 * ?";
    static final String CRON_LEGAL_RESOURCE_TYPE = "0 15 5 1 * ?";

    private final ConceptSubjectService conceptSubjectService;
    private final ConceptSubjectRepository conceptSubjectRepository;
    private final EvidenceTypeService evidenceTypeService;
    private final EvidenceTypeRepository evidenceTypeRepository;
    private final ServiceChannelTypeService serviceChannelTypeService;
    private final ServiceChannelTypeRepository serviceChannelTypeRepository;
    private final RoleTypeService roleTypeService;
    private final RoleTypeRepository roleTypeRepository;
    private final AudienceTypeService audienceTypeService;
    private final AudienceTypeRepository audienceTypeRepository;
    private final RelationshipWithSourceTypeService relationshipWithSourceTypeService;
    private final RelationshipWithSourceTypeRepository relationshipWithSourceTypeRepository;
    private final QualityDimensionService qualityDimensionService;
    private final QualityDimensionRepository qualityDimensionRepository;
    private final LegalResourceTypeService legalResourceTypeService;
    private final LegalResourceTypeRepository legalResourceTypeRepository;

    @Bean
    public ReferenceDataModule conceptSubjectModule() {
        return module("concept-subject", conceptSubjectService, conceptSubjectApi());
    }

    @Bean
    public CodeListApi<ConceptSubject> conceptSubjectApi() {
        return CodeListApis.listWithRdf(
                "/digdir/concept-subjects",
                CodeListRepository.listOnly(conceptSubjectRepository::findAll),
                CodeListApis.sortByUri(ConceptSubject::getUri),
                list -> ConceptSubjects.builder().conceptSubjects(list).build(),
                conceptSubjectService::getRdf,
                ConceptSubject.class);
    }

    @Scheduled(cron = CRON_CONCEPT_SUBJECT)
    public void updateConceptSubjects() {
        conceptSubjectService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule evidenceTypeModule() {
        return module("evidence-type", evidenceTypeService, evidenceTypeApi());
    }

    @Bean
    public CodeListApi<EvidenceType> evidenceTypeApi() {
        return CodeListApis.standard(
                "/digdir/evidence-types",
                CodeListRepository.of(evidenceTypeRepository::findAll, evidenceTypeRepository::findByCode),
                CodeListApis.sortByUri(EvidenceType::getUri),
                list -> EvidenceTypes.builder().evidenceTypes(list).build(),
                evidenceTypeService::getRdf,
                EvidenceType.class);
    }

    @Scheduled(cron = CRON_EVIDENCE_TYPE)
    public void updateEvidenceTypes() {
        evidenceTypeService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule serviceChannelTypeModule() {
        return module("service-channel-type", serviceChannelTypeService, serviceChannelTypeApi());
    }

    @Bean
    public CodeListApi<ServiceChannelType> serviceChannelTypeApi() {
        return CodeListApis.standard(
                "/digdir/service-channel-types",
                CodeListRepository.of(serviceChannelTypeRepository::findAll, serviceChannelTypeRepository::findByCode),
                CodeListApis.sortByUri(ServiceChannelType::getUri),
                list -> ServiceChannelTypes.builder().serviceChannelTypes(list).build(),
                serviceChannelTypeService::getRdf,
                ServiceChannelType.class);
    }

    @Scheduled(cron = CRON_SERVICE_CHANNEL_TYPE)
    public void updateServiceChannelTypes() {
        serviceChannelTypeService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule roleTypeModule() {
        return module("role-type", roleTypeService, roleTypeApi());
    }

    @Bean
    public CodeListApi<RoleType> roleTypeApi() {
        return CodeListApis.standard(
                "/digdir/role-types",
                CodeListRepository.of(roleTypeRepository::findAll, roleTypeRepository::findByCode),
                CodeListApis.sortByUri(RoleType::getUri),
                list -> RoleTypes.builder().roleTypes(list).build(),
                roleTypeService::getRdf,
                RoleType.class);
    }

    @Scheduled(cron = CRON_ROLE_TYPE)
    public void updateRoleTypes() {
        roleTypeService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule audienceTypeModule() {
        return module("audience-type", audienceTypeService, audienceTypeApi());
    }

    @Bean
    public CodeListApi<AudienceType> audienceTypeApi() {
        return CodeListApis.standard(
                "/digdir/audience-types",
                CodeListRepository.of(audienceTypeRepository::findAll, audienceTypeRepository::findByCode),
                CodeListApis.sortByUri(AudienceType::getUri),
                list -> AudienceTypes.builder().audienceTypes(list).build(),
                audienceTypeService::getRdf,
                AudienceType.class);
    }

    @Scheduled(cron = CRON_AUDIENCE_TYPE)
    public void updateAudienceTypes() {
        audienceTypeService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule relationshipWithSourceTypeModule() {
        return module("relationship-with-source-type", relationshipWithSourceTypeService, relationshipWithSourceTypeApi());
    }

    @Bean
    public CodeListApi<RelationshipWithSourceType> relationshipWithSourceTypeApi() {
        return CodeListApis.standard(
                "/digdir/relationship-with-source-types",
                CodeListRepository.of(
                        relationshipWithSourceTypeRepository::findAll,
                        relationshipWithSourceTypeRepository::findByCode),
                CodeListApis.sortByUri(RelationshipWithSourceType::getUri),
                list -> RelationshipWithSourceTypes.builder().relationshipWithSourceTypes(list).build(),
                relationshipWithSourceTypeService::getRdf,
                RelationshipWithSourceType.class);
    }

    @Scheduled(cron = CRON_RELATIONSHIP_WITH_SOURCE_TYPE)
    public void updateRelationshipWithSourceTypes() {
        relationshipWithSourceTypeService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule qualityDimensionModule() {
        return module("quality-dimension", qualityDimensionService, qualityDimensionApi());
    }

    @Bean
    public CodeListApi<QualityDimension> qualityDimensionApi() {
        return CodeListApis.standard(
                "/digdir/quality-dimensions",
                CodeListRepository.of(qualityDimensionRepository::findAll, qualityDimensionRepository::findByCode),
                CodeListApis.sortByUri(QualityDimension::getUri),
                list -> QualityDimensions.builder().qualityDimensions(list).build(),
                qualityDimensionService::getRdf,
                QualityDimension.class);
    }

    @Scheduled(cron = CRON_QUALITY_DIMENSION)
    public void updateQualityDimensions() {
        qualityDimensionService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule legalResourceTypeModule() {
        return module("legal-resource-type", legalResourceTypeService, legalResourceTypeApi());
    }

    @Bean
    public CodeListApi<LegalResourceType> legalResourceTypeApi() {
        return CodeListApis.standard(
                "/digdir/legal-resource-types",
                CodeListRepository.of(legalResourceTypeRepository::findAll, legalResourceTypeRepository::findByCode),
                CodeListApis.sortByUri(LegalResourceType::getUri),
                list -> LegalResourceTypes.builder().legalResourceTypes(list).build(),
                legalResourceTypeService::getRdf,
                LegalResourceType.class);
    }

    @Scheduled(cron = CRON_LEGAL_RESOURCE_TYPE)
    public void updateLegalResourceTypes() {
        legalResourceTypeService.harvestAndSave();
    }

    private static ReferenceDataModule module(String id, HarvestableReferenceData service, CodeListApi<?> api) {
        return new ReferenceDataModule(id, service, api);
    }
}
