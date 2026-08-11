package no.fdk.referencedata.core;

public class HarvestException extends RuntimeException {

    public HarvestException(String message) {
        super(message);
    }

    public HarvestException(String message, Throwable cause) {
        super(message, cause);
    }

    public String reasonTag() {
        return "error";
    }
}
