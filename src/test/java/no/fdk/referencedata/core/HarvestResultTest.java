package no.fdk.referencedata.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HarvestResultTest {

    @Test
    void fromThrowableMapsTypedExceptions() {
        assertEquals("source", HarvestResult.fromThrowable(new HarvestSourceException("x")).reason());
        assertEquals("parse", HarvestResult.fromThrowable(new HarvestParseException("x")).reason());
        assertEquals("persist", HarvestResult.fromThrowable(new HarvestPersistException("x")).reason());
        assertEquals("empty", HarvestResult.fromThrowable(new HarvestEmptyException("x")).reason());
        assertEquals(HarvestResult.Outcome.SKIPPED_EMPTY,
                HarvestResult.fromThrowable(new HarvestEmptyException("x")).outcome());
        assertEquals("error", HarvestResult.fromThrowable(new HarvestException("x")).reason());
        assertEquals("error", HarvestResult.fromThrowable(new RuntimeException("x")).reason());
    }

    @Test
    void fromThrowablePrefersNearestHarvestExceptionOverRootCause() {
        RuntimeException wrapped = new HarvestPersistException("persist failed", new RuntimeException("db down"));
        assertEquals("persist", HarvestResult.fromThrowable(wrapped).reason());
    }

    @Test
    void fromThrowableUnwrapsCause() {
        RuntimeException wrapped = new RuntimeException(new HarvestSourceException("fetch failed"));
        assertEquals("source", HarvestResult.fromThrowable(wrapped).reason());
    }
}
