package no.fdk.referencedata.geonorge.administrativeenheter.nasjon;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

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
public class NasjonControllerIntegrationTest extends AbstractContainerTest {

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

    @Test
    public void test_nasjoner_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/geonorge/administrative-enheter/nasjoner", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(NasjonControllerIntegrationTest.class.getClassLoader().getResource("nasjoner.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
