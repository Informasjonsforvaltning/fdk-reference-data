package no.fdk.referencedata.mobility;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.ReferenceDataModule;
import no.fdk.referencedata.mobility.conditions.MobilityConditionService;
import no.fdk.referencedata.mobility.datastandard.MobilityDataStandardService;
import no.fdk.referencedata.mobility.theme.MobilityThemeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
public class MobilityReferenceDataModules {

    static final String CRON_MOBILITY_THEME = "0 50 4 1 * ?";
    static final String CRON_MOBILITY_CONDITION = "0 55 4 1 * ?";
    static final String CRON_MOBILITY_DATA_STANDARD = "0 0 5 1 * ?";

    private final MobilityThemeService mobilityThemeService;
    private final MobilityConditionService mobilityConditionService;
    private final MobilityDataStandardService mobilityDataStandardService;

    @Bean
    public ReferenceDataModule mobilityThemeModule() {
        return new ReferenceDataModule("mobility-theme", mobilityThemeService);
    }

    @Scheduled(cron = CRON_MOBILITY_THEME)
    public void updateMobilityThemes() {
        mobilityThemeService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule mobilityConditionModule() {
        return new ReferenceDataModule("mobility-condition", mobilityConditionService);
    }

    @Scheduled(cron = CRON_MOBILITY_CONDITION)
    public void updateMobilityCondition() {
        mobilityConditionService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule mobilityDataStandardModule() {
        return new ReferenceDataModule("mobility-data-standard", mobilityDataStandardService);
    }

    @Scheduled(cron = CRON_MOBILITY_DATA_STANDARD)
    public void updateMobilityDataStandards() {
        mobilityDataStandardService.harvestAndSave();
    }
}
