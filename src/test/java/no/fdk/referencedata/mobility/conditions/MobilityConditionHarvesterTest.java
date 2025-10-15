package no.fdk.referencedata.mobility.conditions;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class MobilityConditionHarvesterTest {

    @Test
    public void test_fetch_mobility_conditions() {
        MobilityConditionHarvester harvester = new LocalMobilityConditionHarvester("1.1.0");

        assertNotNull(harvester.getSource("mobility-conditions"));
        assertEquals("mobility-conditions.ttl", harvester.getSource("mobility-conditions").getFilename());
        assertEquals("1.1.0", harvester.getVersion());

        List<MobilityCondition> themes = harvester.harvest().collectList().block();
        assertNotNull(themes);
        assertEquals(10, themes.size());

        MobilityCondition first = themes.get(0);
        assertEquals("https://w3id.org/mobilitydcat-ap/conditions-for-access-and-usage/free-of-charge", first.getUri());
        assertEquals("free-of-charge", first.getCode());
        assertEquals("Free of charge", first.getLabel().get(Language.ENGLISH.code()));
    }

}
