package no.fdk.referencedata.core;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HarvestMetricsTest {

    private SimpleMeterRegistry meterRegistry;
    private HarvestMetrics harvestMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        harvestMetrics = new HarvestMetrics(meterRegistry);
    }

    @Test
    void timedSuccessIncrementsCounterAndUpdatesGauges() {
        HarvestResult result = harvestMetrics.timed("access-right", () -> HarvestResult.success(3));

        assertEquals(HarvestResult.Outcome.SUCCESS, result.outcome());
        assertEquals(1.0, meterRegistry.get(HarvestMetrics.METRIC_TOTAL)
                .tag("module", "access-right")
                .tag("outcome", "success")
                .tag("reason", "none")
                .counter()
                .count());
        assertEquals(3.0, meterRegistry.get(HarvestMetrics.METRIC_ITEMS)
                .tag("module", "access-right")
                .gauge()
                .value());
        assertTrue(meterRegistry.get(HarvestMetrics.METRIC_LAST_SUCCESS)
                .tag("module", "access-right")
                .gauge()
                .value() > 0);
        assertTrue(meterRegistry.get(HarvestMetrics.METRIC_DURATION)
                .tag("module", "access-right")
                .tag("outcome", "success")
                .timer()
                .count() >= 1);
    }

    @Test
    void recordSkippedEmptyDoesNotUpdateSuccessGauges() {
        harvestMetrics.record("media-type", HarvestResult.skippedEmpty(), Duration.ofMillis(5));

        assertEquals(1.0, meterRegistry.get(HarvestMetrics.METRIC_TOTAL)
                .tag("module", "media-type")
                .tag("outcome", "skipped_empty")
                .tag("reason", "empty")
                .counter()
                .count());
        assertEquals(0, meterRegistry.find(HarvestMetrics.METRIC_ITEMS)
                .tag("module", "media-type")
                .gauges()
                .size());
    }

    @Test
    void timedFailureFromResultIncrementsFailureCounter() {
        HarvestResult result = harvestMetrics.timed("geonames", HarvestResult::failure);

        assertEquals(HarvestResult.Outcome.FAILURE, result.outcome());
        assertEquals(1.0, meterRegistry.get(HarvestMetrics.METRIC_TOTAL)
                .tag("module", "geonames")
                .tag("outcome", "failure")
                .tag("reason", "error")
                .counter()
                .count());
    }

    @Test
    void timedRethrowsAndRecordsFailureWhenWorkThrows() {
        assertThrows(IllegalStateException.class, () ->
                harvestMetrics.timed("los", () -> {
                    throw new IllegalStateException("boom");
                }));

        assertEquals(1.0, meterRegistry.get(HarvestMetrics.METRIC_TOTAL)
                .tag("module", "los")
                .tag("outcome", "failure")
                .tag("reason", "error")
                .counter()
                .count());
    }

    @Test
    void incrementPartialErrorIncrementsCounter() {
        harvestMetrics.incrementPartialError("geonames");
        harvestMetrics.incrementPartialError("geonames");

        assertEquals(2.0, meterRegistry.get(HarvestMetrics.METRIC_PARTIAL_ERRORS)
                .tag("module", "geonames")
                .counter()
                .count());
    }
}
