package no.fdk.referencedata.ssb.kommuneorganisasjoner;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

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
public class KommuneOrganisasjonControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private KommuneOrganisasjonRepository kommuneOrganisasjonRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @BeforeEach
    public void setup() {
        KommuneOrganisasjonService kommuneOrganisasjonService = new KommuneOrganisasjonService(
                new LocalKommuneOrganisasjonHarvester(wiremockHost, wiremockPort),
                kommuneOrganisasjonRepository);

        kommuneOrganisasjonService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_kommuner_returns_valid_response() {
        KommuneOrganisasjoner kommuneOrganisasjonr =
                this.restTemplate.getForObject("http://localhost:" + port + "/ssb/kommune-organisasjoner", KommuneOrganisasjoner.class);

        assertEquals(2, kommuneOrganisasjonr.getKommuneOrganisasjoner().size());

        KommuneOrganisasjon first = kommuneOrganisasjonr.getKommuneOrganisasjoner().get(0);
        assertEquals("https://data.brreg.no/enhetsregisteret/api/enheter/817263992", first.getUri());
        assertEquals("KOMMUNE 1 KOMMUNE", first.getOrganisasjonsnavn());
        assertEquals("817263992", first.getOrganisasjonsnummer());
        assertEquals("Kommune 1", first.getKommunenavn());
        assertEquals("3811", first.getKommunenummer());
    }

    @Test
    public void test_if_get_kommune_by_kommunenr_returns_valid_response() {
        KommuneOrganisasjon kommuneOrganisasjon =
                this.restTemplate.getForObject("http://localhost:" + port + "/ssb/kommune-organisasjoner/5053", KommuneOrganisasjon.class);

        assertNotNull(kommuneOrganisasjon);
        assertEquals("https://data.brreg.no/enhetsregisteret/api/enheter/997391853", kommuneOrganisasjon.getUri());
        assertEquals("KOMMUNE 2 KOMMUNE", kommuneOrganisasjon.getOrganisasjonsnavn());
        assertEquals("997391853", kommuneOrganisasjon.getOrganisasjonsnummer());
        assertEquals("Kommune 2", kommuneOrganisasjon.getKommunenavn());
        assertEquals("5053", kommuneOrganisasjon.getKommunenummer());
    }
}
