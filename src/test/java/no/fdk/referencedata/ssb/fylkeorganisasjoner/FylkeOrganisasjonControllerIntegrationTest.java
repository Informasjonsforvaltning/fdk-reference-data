package no.fdk.referencedata.ssb.fylkeorganisasjoner;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;
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
public class FylkeOrganisasjonControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private FylkeOrganisasjonRepository fylkeOrganisasjonRepository;

    private RestClient restClient;

    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        FylkeOrganisasjonService fylkeOrganisasjonService = new FylkeOrganisasjonService(
                new LocalFylkeOrganisasjonHarvester(wiremockHost, wiremockPort),
                fylkeOrganisasjonRepository);

        fylkeOrganisasjonService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_fylker_returns_valid_response() {
        FylkeOrganisasjoner fylkeOrganisasjonr =
                restClient.get().uri("/ssb/fylke-organisasjoner").retrieve().body(FylkeOrganisasjoner.class);

        assertEquals(2, fylkeOrganisasjonr.getFylkeOrganisasjoner().size());

        FylkeOrganisasjon first = fylkeOrganisasjonr.getFylkeOrganisasjoner().get(0);
        assertEquals("https://data.brreg.no/enhetsregisteret/api/enheter/817920632", first.getUri());
        assertEquals("FYLKE 1 FYLKESKOMMUNE", first.getOrganisasjonsnavn());
        assertEquals("817920632", first.getOrganisasjonsnummer());
        assertEquals("Fylke 1", first.getFylkesnavn());
        assertEquals("50", first.getFylkesnummer());
    }

    @Test
    public void test_if_get_fylke_by_fylkenr_returns_valid_response() {
        FylkeOrganisasjon fylkeOrganisasjon =
                restClient.get().uri("/ssb/fylke-organisasjoner/38").retrieve().body(FylkeOrganisasjon.class);

        assertNotNull(fylkeOrganisasjon);
        assertEquals("https://data.brreg.no/enhetsregisteret/api/enheter/821227062", fylkeOrganisasjon.getUri());
        assertEquals("FYLKE 2 FYLKESKOMMUNE", fylkeOrganisasjon.getOrganisasjonsnavn());
        assertEquals("821227062", fylkeOrganisasjon.getOrganisasjonsnummer());
        assertEquals("Fylke 2", fylkeOrganisasjon.getFylkesnavn());
        assertEquals("38", fylkeOrganisasjon.getFylkesnummer());
    }
}
