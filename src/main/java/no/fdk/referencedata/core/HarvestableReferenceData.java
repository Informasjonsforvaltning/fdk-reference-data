package no.fdk.referencedata.core;

public interface HarvestableReferenceData {
    boolean firstTime();

    void harvestAndSave();
}
