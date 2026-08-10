package no.fdk.referencedata.core;

public record ReferenceDataModule(
        String id,
        HarvestableReferenceData service,
        CodeListApi<?> api
) {

    public ReferenceDataModule(String id, HarvestableReferenceData service) {
        this(id, service, null);
    }

    public ReferenceDataModule(String id, CodeListApi<?> api) {
        this(id, null, api);
    }

    public boolean hasApi() {
        return api != null;
    }

    public boolean hasHarvestableService() {
        return service != null;
    }
}
