package no.fdk.referencedata;

import no.fdk.referencedata.core.ReferenceDataModule;
import no.fdk.referencedata.core.ReferenceDataRegistry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Harvests registry modules in tests (expects {@link LocalHarvesterConfiguration} to override harvesters).
 */
public final class HarvestTestSupport {

    private HarvestTestSupport() {}

    public static void harvest(ReferenceDataRegistry registry, String... moduleIds) {
        harvest(registry, Set.copyOf(Arrays.asList(moduleIds)));
    }

    public static void harvest(ReferenceDataRegistry registry, Set<String> moduleIds) {
        Set<String> remaining = new HashSet<>(moduleIds);
        for (ReferenceDataModule module : registry.harvestable()) {
            if (remaining.remove(module.id())) {
                module.service().harvestAndSave();
            }
        }
        if (!remaining.isEmpty()) {
            throw new IllegalArgumentException("Unknown or non-harvestable module ids: " + remaining);
        }
    }
}
