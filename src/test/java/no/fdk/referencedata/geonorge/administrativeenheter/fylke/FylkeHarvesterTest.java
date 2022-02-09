package no.fdk.referencedata.geonorge.administrativeenheter.fylke;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.eu.datatheme.DataTheme;
import no.fdk.referencedata.eu.datatheme.DataThemeHarvester;
import no.fdk.referencedata.eu.datatheme.LocalDataThemeHarvester;
import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
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
public class FylkeHarvesterTest extends AbstractContainerTest {
    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @Test
    public void test_fetch_fylker() {
        LocalFylkeHarvester fylkeHarvester = new LocalFylkeHarvester(wiremockHost, wiremockPort);

        List<Fylke> fylker = fylkeHarvester.harvest().collectList().block();
        assertNotNull(fylker);
        assertEquals(4, fylker.size());

        Fylke first = fylker.get(0);
        assertEquals("https://data.geonorge.no/administrativeEnheter/fylke/id/123456", first.getUri());
        assertEquals("Fylke 1", first.getFylkesnavn());
        assertEquals("123456", first.getFylkesnummer());
    }

}
