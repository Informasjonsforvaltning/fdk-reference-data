package no.fdk.referencedata.core;

public class HarvestEmptyException extends HarvestException {

    public HarvestEmptyException(String message) {
        super(message);
    }

    @Override
    public String reasonTag() {
        return "empty";
    }
}
