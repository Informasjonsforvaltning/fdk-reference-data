package no.fdk.referencedata.eu.plannedavailability;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static no.fdk.referencedata.eu.plannedavailability.LocalPlannedAvailabilityHarvester.PLANNED_AVAILABILITY_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class PlannedAvailabilityHarvesterTest {

    @Test
    public void test_fetch_planned_availabilities() {
        PlannedAvailabilityHarvester harvester = new LocalPlannedAvailabilityHarvester("20220715-0");

        assertNotNull(harvester.getSource());
        assertEquals("planned-availability-sparql-result.ttl", harvester.getSource().getFilename());
        assertEquals("20220715-0", harvester.getVersion());

        List<PlannedAvailability> availabilities = harvester.harvest().collectList().block();
        assertNotNull(availabilities);
        assertEquals(PLANNED_AVAILABILITY_SIZE, availabilities.size());

        PlannedAvailability first = availabilities.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/planned-availability/STABLE", first.getUri());
        assertEquals("STABLE", first.getCode());
        assertEquals("stable", first.getLabel().get(Language.ENGLISH.code()));
    }

}
