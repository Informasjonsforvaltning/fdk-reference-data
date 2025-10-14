package no.fdk.referencedata.mobility.theme;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class MobilityThemeHarvesterTest {

    @Test
    public void test_fetch_mobility_themes() {
        MobilityThemeHarvester harvester = new LocalMobilityThemeHarvester("1.0.0");

        assertNotNull(harvester.getSource("mobility-themes"));
        assertEquals("mobility-themes.ttl", harvester.getSource("mobility-themes").getFilename());
        assertEquals("1.0.0", harvester.getVersion());

        List<MobilityTheme> themes = harvester.harvest().collectList().block();
        assertNotNull(themes);
        assertEquals(123, themes.size());

        MobilityTheme first = themes.get(0);
        assertEquals("https://w3id.org/mobilitydcat-ap/mobility-theme/real-time-estimated-departure-and-arrival-times", first.getUri());
        assertEquals("real-time-estimated-departure-and-arrival-times", first.getCode());
        assertEquals("Real-time estimated departure and arrival times", first.getLabel().get(Language.ENGLISH.code()));
    }

}
