package no.fdk.referencedata.eu.mainactivity;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static no.fdk.referencedata.LocalHarvestFixtures.MAIN_ACTIVITIES_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class MainActivityHarvesterTest {

    @Test
    public void test_fetch_access_rights() {
        MainActivityHarvester harvester = LocalHarvesters.mainActivity();

        assertNotNull(harvester.getSource());
        assertEquals("main-activity-sparql-result.ttl", harvester.getSource().getFilename());

        List<MainActivity> mainActivities = harvester.harvest().collectList().block();
        assertNotNull(mainActivities);
        assertEquals(MAIN_ACTIVITIES_SIZE, mainActivities.size());

        MainActivity first = mainActivities.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/main-activity/health", first.getUri());
        assertEquals("health", first.getCode());
        assertEquals("Health", first.getLabel().get(Language.ENGLISH.code()));
    }

}
