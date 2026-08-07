package no.fdk.referencedata.core;

public record ReferenceDataModule(
        String id,
        ScheduleSpec schedule,
        HarvestableReferenceData service
) {}
