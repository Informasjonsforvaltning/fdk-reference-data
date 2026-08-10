package no.fdk.referencedata.core;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReferenceDataRegistry {

    private final List<ReferenceDataModule> modules;

    public ReferenceDataRegistry(List<ReferenceDataModule> modules) {
        this.modules = List.copyOf(modules);
    }

    public List<ReferenceDataModule> all() {
        return modules;
    }

    public List<ReferenceDataModule> withApi() {
        return modules.stream()
                .filter(ReferenceDataModule::hasApi)
                .toList();
    }

    public List<ReferenceDataModule> harvestable() {
        return modules.stream()
                .filter(ReferenceDataModule::hasHarvestableService)
                .toList();
    }
}
