package no.fdk.referencedata.core;

public record HarvestResult(Outcome outcome, int itemCount, String reason) {

    public enum Outcome {
        SUCCESS,
        FAILURE,
        SKIPPED_EMPTY
    }

    public static HarvestResult success(int itemCount) {
        return new HarvestResult(Outcome.SUCCESS, itemCount, null);
    }

    public static HarvestResult failure() {
        return new HarvestResult(Outcome.FAILURE, 0, "error");
    }

    public static HarvestResult failure(String reason) {
        return new HarvestResult(Outcome.FAILURE, 0, reason);
    }

    public static HarvestResult skippedEmpty() {
        return new HarvestResult(Outcome.SKIPPED_EMPTY, 0, "empty");
    }
}
