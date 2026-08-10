package no.fdk.referencedata.core;

public record ReferenceDataModule(
        String id,
        HarvestableReferenceData service
) {}
