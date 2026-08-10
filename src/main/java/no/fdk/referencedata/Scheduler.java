package no.fdk.referencedata;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Scheduler {

    private final ReferenceDataRegistry registry;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        registry.harvestable().stream()
                .filter(module -> module.service().firstTime())
                .forEach(module -> module.service().harvestAndSave());
    }
}
