package no.fdk.referencedata.mobility;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListApi;
import no.fdk.referencedata.core.CodeListApis;
import no.fdk.referencedata.core.CodeListRepository;
import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataModule;
import no.fdk.referencedata.mobility.conditions.MobilityCondition;
import no.fdk.referencedata.mobility.conditions.MobilityConditionRepository;
import no.fdk.referencedata.mobility.conditions.MobilityConditionService;
import no.fdk.referencedata.mobility.conditions.MobilityConditions;
import no.fdk.referencedata.mobility.datastandard.MobilityDataStandard;
import no.fdk.referencedata.mobility.datastandard.MobilityDataStandardRepository;
import no.fdk.referencedata.mobility.datastandard.MobilityDataStandardService;
import no.fdk.referencedata.mobility.datastandard.MobilityDataStandards;
import no.fdk.referencedata.mobility.theme.MobilityTheme;
import no.fdk.referencedata.mobility.theme.MobilityThemeRepository;
import no.fdk.referencedata.mobility.theme.MobilityThemeService;
import no.fdk.referencedata.mobility.theme.MobilityThemes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.fdk.referencedata.core.HarvestCron.*;

@Configuration
@RequiredArgsConstructor
public class MobilityReferenceDataModules {

    private final MobilityThemeService mobilityThemeService;
    private final MobilityThemeRepository mobilityThemeRepository;
    private final MobilityConditionService mobilityConditionService;
    private final MobilityConditionRepository mobilityConditionRepository;
    private final MobilityDataStandardService mobilityDataStandardService;
    private final MobilityDataStandardRepository mobilityDataStandardRepository;

    @Bean
    public ReferenceDataModule mobilityThemeModule() {
        return module("mobility-theme", mobilityThemeService, mobilityThemeApi(), CRON_MOBILITY_THEME);
    }

    @Bean
    public CodeListApi<MobilityTheme> mobilityThemeApi() {
        return CodeListApis.standard(
                "/mobility/themes",
                CodeListRepository.of(mobilityThemeRepository::findAll, mobilityThemeRepository::findByCode),
                CodeListApis.sortByUri(MobilityTheme::getUri),
                list -> MobilityThemes.builder().mobilityThemes(list).build(),
                mobilityThemeService::getRdf,
                MobilityTheme.class);
    }

    @Bean
    public ReferenceDataModule mobilityConditionModule() {
        return module("mobility-condition", mobilityConditionService, mobilityConditionApi(), CRON_MOBILITY_CONDITION);
    }

    @Bean
    public CodeListApi<MobilityCondition> mobilityConditionApi() {
        return CodeListApis.standard(
                "/mobility/conditions-for-access-and-usage",
                CodeListRepository.of(mobilityConditionRepository::findAll, mobilityConditionRepository::findByCode),
                CodeListApis.sortByUri(MobilityCondition::getUri),
                list -> MobilityConditions.builder().mobilityConditions(list).build(),
                mobilityConditionService::getRdf,
                MobilityCondition.class);
    }

    @Bean
    public ReferenceDataModule mobilityDataStandardModule() {
        return module("mobility-data-standard", mobilityDataStandardService, mobilityDataStandardApi(), CRON_MOBILITY_DATA_STANDARD);
    }

    @Bean
    public CodeListApi<MobilityDataStandard> mobilityDataStandardApi() {
        return CodeListApis.standard(
                "/mobility/data-standards",
                CodeListRepository.of(mobilityDataStandardRepository::findAll, mobilityDataStandardRepository::findByCode),
                CodeListApis.sortByUri(MobilityDataStandard::getUri),
                list -> MobilityDataStandards.builder().mobilityDataStandards(list).build(),
                mobilityDataStandardService::getRdf,
                MobilityDataStandard.class);
    }

    private static ReferenceDataModule module(String id, HarvestableReferenceData service, CodeListApi<?> api, String cron) {
        return new ReferenceDataModule(id, service, api, cron);
    }
}
