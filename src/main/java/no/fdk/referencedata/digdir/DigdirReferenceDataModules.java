package no.fdk.referencedata.digdir;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.ReferenceDataModule;
import no.fdk.referencedata.digdir.audiencetype.AudienceTypeService;
import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubjectService;
import no.fdk.referencedata.digdir.evidencetype.EvidenceTypeService;
import no.fdk.referencedata.digdir.legalresourcetype.LegalResourceTypeService;
import no.fdk.referencedata.digdir.qualitydimension.QualityDimensionService;
import no.fdk.referencedata.digdir.relationshipwithsourcetype.RelationshipWithSourceTypeService;
import no.fdk.referencedata.digdir.roletype.RoleTypeService;
import no.fdk.referencedata.digdir.servicechanneltype.ServiceChannelTypeService;
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
    private final EvidenceTypeService evidenceTypeService;
    private final ServiceChannelTypeService serviceChannelTypeService;
    private final RoleTypeService roleTypeService;
    private final AudienceTypeService audienceTypeService;
    private final RelationshipWithSourceTypeService relationshipWithSourceTypeService;
    private final QualityDimensionService qualityDimensionService;
    private final LegalResourceTypeService legalResourceTypeService;

    @Bean
    public ReferenceDataModule conceptSubjectModule() {
        return new ReferenceDataModule("concept-subject", conceptSubjectService);
    }

    @Scheduled(cron = CRON_CONCEPT_SUBJECT)
    public void updateConceptSubjects() {
        conceptSubjectService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule evidenceTypeModule() {
        return new ReferenceDataModule("evidence-type", evidenceTypeService);
    }

    @Scheduled(cron = CRON_EVIDENCE_TYPE)
    public void updateEvidenceTypes() {
        evidenceTypeService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule serviceChannelTypeModule() {
        return new ReferenceDataModule("service-channel-type", serviceChannelTypeService);
    }

    @Scheduled(cron = CRON_SERVICE_CHANNEL_TYPE)
    public void updateServiceChannelTypes() {
        serviceChannelTypeService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule roleTypeModule() {
        return new ReferenceDataModule("role-type", roleTypeService);
    }

    @Scheduled(cron = CRON_ROLE_TYPE)
    public void updateRoleTypes() {
        roleTypeService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule audienceTypeModule() {
        return new ReferenceDataModule("audience-type", audienceTypeService);
    }

    @Scheduled(cron = CRON_AUDIENCE_TYPE)
    public void updateAudienceTypes() {
        audienceTypeService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule relationshipWithSourceTypeModule() {
        return new ReferenceDataModule("relationship-with-source-type", relationshipWithSourceTypeService);
    }

    @Scheduled(cron = CRON_RELATIONSHIP_WITH_SOURCE_TYPE)
    public void updateRelationshipWithSourceTypes() {
        relationshipWithSourceTypeService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule qualityDimensionModule() {
        return new ReferenceDataModule("quality-dimension", qualityDimensionService);
    }

    @Scheduled(cron = CRON_QUALITY_DIMENSION)
    public void updateQualityDimensions() {
        qualityDimensionService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule legalResourceTypeModule() {
        return new ReferenceDataModule("legal-resource-type", legalResourceTypeService);
    }

    @Scheduled(cron = CRON_LEGAL_RESOURCE_TYPE)
    public void updateLegalResourceTypes() {
        legalResourceTypeService.harvestAndSave();
    }
}
