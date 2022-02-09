package no.fdk.referencedata.geonorge.administrativeenheter.kommune;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.Fylke;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.LocalFylkeHarvester;
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
                "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class KommuneHarvesterTest extends AbstractContainerTest {
    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @Test
    public void test_fetch_kommuner() {
        LocalKommuneHarvester kommuneHarvester = new LocalKommuneHarvester(wiremockHost, wiremockPort);

        List<Kommune> kommuner = kommuneHarvester.harvest().collectList().block();
        assertNotNull(kommuner);
        assertEquals(4, kommuner.size());

        Kommune first = kommuner.get(0);
        assertEquals("https://data.geonorge.no/administrativeEnheter/kommune/id/123456", first.getUri());
        assertEquals("Kommune 1", first.getKommunenavn());
        assertEquals("Kommune 1 norsk", first.getKommunenavnNorsk());
        assertEquals("123456", first.getKommunenummer());
    }

}
