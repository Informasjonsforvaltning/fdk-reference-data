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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SchedulerTest {

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
                "0 0 1 1 * ?");
        ReferenceDataModule unscheduled = new ReferenceDataModule(
                "adms-status",
                null,
                api(false),
                null);
        Scheduler scheduler = new Scheduler(new ReferenceDataRegistry(List.of(scheduled, unscheduled)));

        scheduler.configureTasks(taskRegistrar);

        verify(taskRegistrar).addTriggerTask(any(Runnable.class), any(Trigger.class));
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
