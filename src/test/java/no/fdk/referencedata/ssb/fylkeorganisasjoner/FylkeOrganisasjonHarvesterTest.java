package no.fdk.referencedata.ssb.fylkeorganisasjoner;

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
                "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class FylkeOrganisasjonHarvesterTest extends AbstractContainerTest {
    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @Test
    public void test_fetch_fylke_organisasjonr() {
        LocalFylkeOrganisasjonHarvester fylkeOrganisasjonHarvester = new LocalFylkeOrganisasjonHarvester(wiremockHost, wiremockPort);

        List<FylkeOrganisasjon> fylkeOrganisasjoner = fylkeOrganisasjonHarvester.harvest().collectList().block();
        assertNotNull(fylkeOrganisasjoner);
        assertEquals(2, fylkeOrganisasjoner.size());

        FylkeOrganisasjon first = fylkeOrganisasjoner.get(0);
        assertEquals("https://data.brreg.no/enhetsregisteret/api/enheter/817920632", first.getUri());
        assertEquals("FYLKE 1 FYLKESKOMMUNE", first.getOrganisasjonsnavn());
        assertEquals("817920632", first.getOrganisasjonsnummer());
        assertEquals("Fylke 1", first.getFylkesnavn());
        assertEquals("50", first.getFylkesnummer());
    }

}
