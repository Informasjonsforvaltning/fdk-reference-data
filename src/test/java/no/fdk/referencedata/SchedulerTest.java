package no.fdk.referencedata;

import no.fdk.referencedata.core.CodeListApi;
import no.fdk.referencedata.core.CodeListRepository;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.HarvestTrigger;
import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataModule;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SchedulerTest {

    private static final String CRON_EXPRESSION = "0 0 1 1 * ?";

    @Mock
    private HarvestableReferenceData harvestableWithCron;
    @Mock
    private HarvestableReferenceData harvestableWithoutCron;
    @Mock
    private ScheduledTaskRegistrar taskRegistrar;

    @Test
    void configureTasksRegistersOnlyModulesWithCron() {
        ReferenceDataModule scheduled = new ReferenceDataModule(
                "access-right",
                harvestableWithCron,
                api(true),
                CRON_EXPRESSION);
        ReferenceDataModule unscheduled = new ReferenceDataModule(
                "adms-status",
                null,
                api(false),
                null);
        ReferenceDataModule harvestableApiOnly = new ReferenceDataModule(
                "media-type",
                harvestableWithoutCron,
                null,
                null);
        Scheduler scheduler = new Scheduler(new ReferenceDataRegistry(
                List.of(scheduled, unscheduled, harvestableApiOnly)));

        scheduler.configureTasks(taskRegistrar);

        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        verify(taskRegistrar, times(1)).addTriggerTask(any(Runnable.class), triggerCaptor.capture());
        assertThat(triggerCaptor.getValue()).isInstanceOf(CronTrigger.class);
        assertEquals(CRON_EXPRESSION, ((CronTrigger) triggerCaptor.getValue()).getExpression());
    }

    @Test
    void configureTasksSkipsModulesWithoutCron() {
        ReferenceDataModule unscheduled = new ReferenceDataModule(
                "media-type",
                harvestableWithoutCron,
                null,
                null);
        Scheduler scheduler = new Scheduler(new ReferenceDataRegistry(List.of(unscheduled)));

        scheduler.configureTasks(taskRegistrar);

        verify(taskRegistrar, never()).addTriggerTask(any(Runnable.class), any(Trigger.class));
    }

    @Test
    void registeredCronTaskRunsWithCronTrigger() {
        AtomicReference<String> seenTrigger = new AtomicReference<>();
        HarvestableReferenceData harvestable = new HarvestableReferenceData() {
            @Override
            public boolean firstTime() {
                return false;
            }

            @Override
            public HarvestResult harvestAndSave() {
                seenTrigger.set(HarvestTrigger.current());
                return HarvestResult.success(1);
            }
        };
        ReferenceDataModule module = new ReferenceDataModule("access-right", harvestable, CRON_EXPRESSION);
        Scheduler scheduler = new Scheduler(new ReferenceDataRegistry(List.of(module)));

        ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);
        scheduler.configureTasks(taskRegistrar);
        verify(taskRegistrar).addTriggerTask(taskCaptor.capture(), any(Trigger.class));

        taskCaptor.getValue().run();

        assertEquals(HarvestTrigger.CRON, seenTrigger.get());
        assertEquals(HarvestTrigger.UNKNOWN, HarvestTrigger.current());
    }

    @Test
    void initRunsFirstTimeModulesWithStartupTrigger() {
        AtomicReference<String> seenTrigger = new AtomicReference<>();
        HarvestableReferenceData firstTime = new HarvestableReferenceData() {
            @Override
            public boolean firstTime() {
                return true;
            }

            @Override
            public HarvestResult harvestAndSave() {
                seenTrigger.set(HarvestTrigger.current());
                return HarvestResult.success(1);
            }
        };
        HarvestableReferenceData alreadySeeded = new HarvestableReferenceData() {
            @Override
            public boolean firstTime() {
                return false;
            }

            @Override
            public HarvestResult harvestAndSave() {
                throw new AssertionError("should not harvest");
            }
        };
        Scheduler scheduler = new Scheduler(new ReferenceDataRegistry(List.of(
                new ReferenceDataModule("access-right", firstTime, CRON_EXPRESSION),
                new ReferenceDataModule("media-type", alreadySeeded))));

        scheduler.init();

        assertEquals(HarvestTrigger.STARTUP, seenTrigger.get());
        assertEquals(HarvestTrigger.UNKNOWN, HarvestTrigger.current());
    }

    @Test
    void runSetsTriggerContext() {
        AtomicReference<String> seenTrigger = new AtomicReference<>();
        HarvestableReferenceData harvestable = new HarvestableReferenceData() {
            @Override
            public boolean firstTime() {
                return false;
            }

            @Override
            public HarvestResult harvestAndSave() {
                seenTrigger.set(HarvestTrigger.current());
                return HarvestResult.success(1);
            }
        };
        ReferenceDataModule module = new ReferenceDataModule("access-right", harvestable, "0 0 * * * ?");
        Scheduler scheduler = new Scheduler(new ReferenceDataRegistry(List.of(module)));

        HarvestResult result = scheduler.run(module, HarvestTrigger.API);

        assertTrue(result.isSuccess());
        assertEquals(HarvestTrigger.API, seenTrigger.get());
        assertEquals(HarvestTrigger.UNKNOWN, HarvestTrigger.current());
    }

    @Test
    void schedulerBeanAbsentWhenSchedulingDisabled() {
        new ApplicationContextRunner()
                .withUserConfiguration(Scheduler.class, EmptyRegistryConfig.class)
                .withPropertyValues("scheduling.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(Scheduler.class));
    }

    @Test
    void schedulerBeanPresentWhenSchedulingEnabled() {
        new ApplicationContextRunner()
                .withUserConfiguration(Scheduler.class, EmptyRegistryConfig.class)
                .withPropertyValues("scheduling.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(Scheduler.class));
    }

    @Configuration
    static class EmptyRegistryConfig {
        @Bean
        ReferenceDataRegistry referenceDataRegistry() {
            return new ReferenceDataRegistry(List.of());
        }
    }

    private static CodeListApi<String> api(boolean supportsHarvestPost) {
        return new CodeListApi<>(
                "/test",
                CodeListRepository.of(List::of, code -> Optional.empty()),
                null,
                list -> list,
                String.class,
                null,
                supportsHarvestPost,
                false,
                "code");
    }
}
