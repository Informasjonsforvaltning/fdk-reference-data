package no.fdk.referencedata;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.HarvestTrigger;
import no.fdk.referencedata.core.ReferenceDataModule;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Scheduler implements SchedulingConfigurer {

    private final ReferenceDataRegistry registry;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        registry.harvestable().stream()
                .filter(ReferenceDataModule::hasCron)
                .forEach(module -> taskRegistrar.addTriggerTask(
                        () -> run(module, HarvestTrigger.CRON),
                        new CronTrigger(module.cron())));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        registry.harvestable().stream()
                .filter(module -> module.service().firstTime())
                .forEach(module -> run(module, HarvestTrigger.STARTUP));
    }

    HarvestResult run(ReferenceDataModule module, String trigger) {
        return HarvestTrigger.call(trigger, () -> module.service().harvestAndSave());
    }
}
