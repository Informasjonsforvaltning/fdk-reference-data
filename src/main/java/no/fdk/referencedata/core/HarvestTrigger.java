package no.fdk.referencedata.core;

import java.util.function.Supplier;

/**
 * Propagates the harvest trigger ({@code cron}, {@code startup}, {@code api}) to
 * {@link HarvestMetrics} without changing every service signature.
 */
public final class HarvestTrigger {

    public static final String CRON = "cron";
    public static final String STARTUP = "startup";
    public static final String API = "api";
    public static final String UNKNOWN = "unknown";

    private static final ThreadLocal<String> CURRENT = ThreadLocal.withInitial(() -> UNKNOWN);

    private HarvestTrigger() {
    }

    public static String current() {
        return CURRENT.get();
    }

    public static HarvestResult call(String trigger, Supplier<HarvestResult> work) {
        String previous = CURRENT.get();
        CURRENT.set(trigger);
        try {
            return work.get();
        } finally {
            CURRENT.set(previous);
        }
    }
}
