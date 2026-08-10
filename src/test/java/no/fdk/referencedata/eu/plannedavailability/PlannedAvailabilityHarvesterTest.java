package no.fdk.referencedata.eu.plannedavailability;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static no.fdk.referencedata.LocalHarvestFixtures.PLANNED_AVAILABILITY_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class PlannedAvailabilityHarvesterTest {

    @Test
    public void test_fetch_planned_availabilities() {
        PlannedAvailabilityHarvester harvester = LocalHarvesters.plannedAvailability();

        assertNotNull(harvester.getSource());
        assertEquals("planned-availability-sparql-result.ttl", harvester.getSource().getFilename());

        List<PlannedAvailability> availabilities = harvester.harvest().collectList().block();
        assertNotNull(availabilities);
        assertEquals(PLANNED_AVAILABILITY_SIZE, availabilities.size());

        PlannedAvailability first = availabilities.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/planned-availability/STABLE", first.getUri());
        assertEquals("STABLE", first.getCode());
        assertEquals("stable", first.getLabel().get(Language.ENGLISH.code()));
    }

}
