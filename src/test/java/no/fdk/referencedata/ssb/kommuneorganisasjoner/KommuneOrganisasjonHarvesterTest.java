package no.fdk.referencedata.ssb.kommuneorganisasjoner;

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
public class KommuneOrganisasjonHarvesterTest extends AbstractContainerTest {
    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @Test
    public void test_fetch_kommune_organisasjonr() {
        LocalKommuneOrganisasjonHarvester kommuneOrganisasjonHarvester = new LocalKommuneOrganisasjonHarvester(wiremockHost, wiremockPort);

        List<KommuneOrganisasjon> kommuneOrganisasjoner = kommuneOrganisasjonHarvester.harvest().collectList().block();
        assertNotNull(kommuneOrganisasjoner);
        assertEquals(2, kommuneOrganisasjoner.size());

        KommuneOrganisasjon first = kommuneOrganisasjoner.get(0);
        assertEquals("https://data.brreg.no/enhetsregisteret/api/enheter/817263992", first.getUri());
        assertEquals("KOMMUNE 1 KOMMUNE", first.getOrganisasjonsnavn());
        assertEquals("817263992", first.getOrganisasjonsnummer());
        assertEquals("Kommune 1", first.getKommunenavn());
        assertEquals("3811", first.getKommunenummer());
    }

}
