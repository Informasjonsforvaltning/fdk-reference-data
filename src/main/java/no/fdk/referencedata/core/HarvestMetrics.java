package no.fdk.referencedata.core;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Harvest-domain Prometheus metrics (scraped via Actuator {@code /actuator/prometheus}).
 *
 * <ul>
 *   <li>{@code reference_data_harvest_total{module,outcome,reason,trigger}}</li>
 *   <li>{@code reference_data_harvest_duration_seconds{module,outcome,trigger}}</li>
 *   <li>{@code reference_data_harvest_items{module}}</li>
 *   <li>{@code reference_data_harvest_last_success_timestamp{module}}</li>
 *   <li>{@code reference_data_harvest_partial_errors_total{module}}</li>
 * </ul>
 *
 * Trigger is taken from {@link HarvestTrigger} ({@code cron}, {@code startup}, {@code api}).
 */
@Component
public class HarvestMetrics {

    static final String METRIC_TOTAL = "reference_data_harvest_total";
    static final String METRIC_DURATION = "reference_data_harvest_duration_seconds";
    static final String METRIC_ITEMS = "reference_data_harvest_items";
    static final String METRIC_LAST_SUCCESS = "reference_data_harvest_last_success_timestamp";
    static final String METRIC_PARTIAL_ERRORS = "reference_data_harvest_partial_errors_total";

    private static final String REASON_NONE = "none";

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, AtomicInteger> itemCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> lastSuccessTimestamps = new ConcurrentHashMap<>();

    public HarvestMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public HarvestResult timed(String moduleId, Supplier<HarvestResult> work) {
        return timed(moduleId, HarvestTrigger.current(), work);
    }

    public HarvestResult timed(String moduleId, String trigger, Supplier<HarvestResult> work) {
        Instant start = Instant.now();
        try {
            HarvestResult result = work.get();
            record(moduleId, trigger, result, Duration.between(start, Instant.now()));
            return result;
        } catch (RuntimeException e) {
            record(moduleId, trigger, HarvestResult.failure(), Duration.between(start, Instant.now()));
            throw e;
        }
    }

    public void record(String moduleId, HarvestResult result, Duration duration) {
        record(moduleId, HarvestTrigger.current(), result, duration);
    }

    public void record(String moduleId, String trigger, HarvestResult result, Duration duration) {
        String outcome = outcomeTag(result.outcome());
        String reason = result.reason() != null ? result.reason() : REASON_NONE;
        String triggerTag = trigger != null ? trigger : HarvestTrigger.UNKNOWN;

        meterRegistry.counter(
                        METRIC_TOTAL,
                        Tags.of(
                                "module", moduleId,
                                "outcome", outcome,
                                "reason", reason,
                                "trigger", triggerTag))
                .increment();

        Timer.builder(METRIC_DURATION)
                .tags(Tags.of("module", moduleId, "outcome", outcome, "trigger", triggerTag))
                .register(meterRegistry)
                .record(duration);

        if (result.outcome() == HarvestResult.Outcome.SUCCESS) {
            itemCountGauge(moduleId).set(result.itemCount());
            lastSuccessGauge(moduleId).set(Instant.now().getEpochSecond());
        }
    }

    public void incrementPartialError(String moduleId) {
        meterRegistry.counter(METRIC_PARTIAL_ERRORS, Tags.of("module", moduleId)).increment();
    }

    private AtomicInteger itemCountGauge(String moduleId) {
        return itemCounts.computeIfAbsent(moduleId, id -> {
            AtomicInteger value = new AtomicInteger();
            meterRegistry.gauge(METRIC_ITEMS, Tags.of("module", id), value);
            return value;
        });
    }

    private AtomicLong lastSuccessGauge(String moduleId) {
        return lastSuccessTimestamps.computeIfAbsent(moduleId, id -> {
            AtomicLong value = new AtomicLong();
            meterRegistry.gauge(METRIC_LAST_SUCCESS, Tags.of("module", id), value);
            return value;
        });
    }

    private static String outcomeTag(HarvestResult.Outcome outcome) {
        return outcome.name().toLowerCase(Locale.ROOT);
    }
}
