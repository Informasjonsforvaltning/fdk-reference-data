package no.fdk.referencedata.geonorge.administrativeenheter.nasjon;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.Kommune;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneRepository;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneService;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.Kommuner;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "wiremock.host=dummy",
            "wiremock.port=0"
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class NasjonControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test_if_get_all_nasjoner_returns_valid_response() {
        Nasjoner nasjoner =
                this.restTemplate.getForObject("http://localhost:" + port + "/geonorge/administrative-enheter/nasjoner", Nasjoner.class);

        assertEquals(1, nasjoner.getNasjoner().size());

        Nasjon first = nasjoner.getNasjoner().get(0);
        assertEquals("https://data.geonorge.no/administrativeEnheter/nasjon/id/173163", first.getUri());
        assertEquals("Norge", first.getNasjonsnavn());
        assertEquals("173163", first.getNasjonsnummer());
    }

    @Test
    public void test_if_get_nasjon_by_nasjonsnr_returns_valid_response() {
        Nasjon nasjon =
                this.restTemplate.getForObject("http://localhost:" + port + "/geonorge/administrative-enheter/nasjoner/173163", Nasjon.class);

        assertNotNull(nasjon);
        assertEquals("https://data.geonorge.no/administrativeEnheter/nasjon/id/173163", nasjon.getUri());
        assertEquals("Norge", nasjon.getNasjonsnavn());
        assertEquals("173163", nasjon.getNasjonsnummer());
    }
}
