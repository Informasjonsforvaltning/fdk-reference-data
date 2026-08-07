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
}
