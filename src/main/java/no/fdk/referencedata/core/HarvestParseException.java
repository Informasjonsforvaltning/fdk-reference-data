package no.fdk.referencedata.core;

public class HarvestParseException extends HarvestException {

    public HarvestParseException(String message) {
        super(message);
    }

    public HarvestParseException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String reasonTag() {
        return "parse";
    }
}
