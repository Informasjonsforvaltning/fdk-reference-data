package no.fdk.referencedata.eu.mainactivity;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class MainActivityHarvesterTest {

    @Test
    public void test_fetch_access_rights() {
        MainActivityHarvester harvester = new LocalMainActivityHarvester("123");

        assertNotNull(harvester.getSource("", ""));
        assertEquals("main-activity-skos.rdf", harvester.getSource("", "").getFilename());
        assertEquals("123", harvester.getVersion());

        List<MainActivity> mainActivities = harvester.harvest().collectList().block();
        assertNotNull(mainActivities);
        assertEquals(21, mainActivities.size());

        MainActivity first = mainActivities.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/main-activity/gas-heat", first.getUri());
        assertEquals("gas-heat", first.getCode());
        assertEquals("Production, transport or distribution of gas or heat", first.getLabel().get(Language.ENGLISH.code()));
    }

}
