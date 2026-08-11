package no.fdk.referencedata.core;

public class HarvestPersistException extends HarvestException {

    public HarvestPersistException(String message) {
        super(message);
    }

    public HarvestPersistException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String reasonTag() {
        return "persist";
    }
}
