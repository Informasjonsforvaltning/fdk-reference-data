package no.fdk.referencedata.core;

public record ReferenceDataModule(
        String id,
        HarvestableReferenceData service,
        CodeListApi<?> api,
        String cron
) {

    public ReferenceDataModule(String id, HarvestableReferenceData service, CodeListApi<?> api) {
        this(id, service, api, null);
    }

    public ReferenceDataModule(String id, HarvestableReferenceData service) {
        this(id, service, null, null);
    }

    public ReferenceDataModule(String id, HarvestableReferenceData service, String cron) {
        this(id, service, null, cron);
    }

    public ReferenceDataModule(String id, CodeListApi<?> api) {
        this(id, null, api, null);
    }

    public boolean hasApi() {
        return api != null;
    }

    public boolean hasHarvestableService() {
        return service != null;
    }

    public boolean hasCron() {
        return cron != null && !cron.isBlank();
    }
}
