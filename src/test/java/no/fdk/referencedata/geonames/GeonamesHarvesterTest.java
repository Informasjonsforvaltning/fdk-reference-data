package no.fdk.referencedata.geonames;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class GeonamesHarvesterTest extends AbstractContainerTest {

    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @Test
    public void test_fetch_fylker() {
        LocalGeonamesHarvester harvester = new LocalGeonamesHarvester(wiremockHost, wiremockPort);

        List<GeonamesFylke> fylker = harvester.harvestFylker().collectList().block();
        assertNotNull(fylker);
        assertEquals(2, fylker.size());

        fylker.sort((a, b) -> a.getName().compareTo(b.getName()));
        GeonamesFylke first = fylker.get(0);
        assertEquals("https://sws.geonames.org/7626836/", first.getUri());
        assertEquals("7626836", first.getGeonameId());
        assertEquals("Agder", first.getName());
    }

    @Test
    public void test_fetch_kommuner_for_fylke() {
        LocalGeonamesHarvester harvester = new LocalGeonamesHarvester(wiremockHost, wiremockPort);

        List<GeonamesKommune> kommuner = harvester.harvestKommunerForFylke("7626836").collectList().block();
        assertNotNull(kommuner);
        assertEquals(2, kommuner.size());

        kommuner.sort((a, b) -> a.getName().compareTo(b.getName()));
        GeonamesKommune first = kommuner.get(0);
        assertEquals("https://sws.geonames.org/7626838/", first.getUri());
        assertEquals("7626838", first.getGeonameId());
        assertEquals("Arendal", first.getName());
        assertEquals("7626836", first.getFylkeGeonameId());
    }
}
