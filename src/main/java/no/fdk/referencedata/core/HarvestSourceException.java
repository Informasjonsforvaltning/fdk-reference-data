package no.fdk.referencedata.core;

public class HarvestSourceException extends HarvestException {

    public HarvestSourceException(String message) {
        super(message);
    }

    public HarvestSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String reasonTag() {
        return "source";
    }
}
