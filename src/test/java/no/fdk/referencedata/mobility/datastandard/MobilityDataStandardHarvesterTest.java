package no.fdk.referencedata.mobility.datastandard;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class MobilityDataStandardHarvesterTest {

    @Test
    public void test_fetch_mobility_data_standards() {
        MobilityDataStandardHarvester harvester = new LocalMobilityDataStandardHarvester("1.1.0");

        assertNotNull(harvester.getSource("mobility-data-standards"));
        assertEquals("mobility-data-standards.ttl", harvester.getSource("mobility-data-standards").getFilename());
        assertEquals("1.1.0", harvester.getVersion());

        List<MobilityDataStandard> standards = harvester.harvest().collectList().block();
        assertNotNull(standards);
        assertEquals(15, standards.size());

        MobilityDataStandard first = standards.get(0);
        assertEquals("https://w3id.org/mobilitydcat-ap/mobility-data-standard/netex", first.getUri());
        assertEquals("netex", first.getCode());
        assertEquals("NeTEx", first.getLabel().get(Language.ENGLISH.code()));
    }

}
