package no.fdk.referencedata.core;

public record ReferenceDataModule(
        String id,
        HarvestableReferenceData service,
        CodeListApi<?> api
) {

    public ReferenceDataModule(String id, HarvestableReferenceData service) {
        this(id, service, null);
    }

    public boolean hasApi() {
        return api != null;
    }
}
