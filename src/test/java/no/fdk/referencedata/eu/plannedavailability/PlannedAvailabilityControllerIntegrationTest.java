package no.fdk.referencedata.eu.plannedavailability;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class PlannedAvailabilityControllerIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        HarvestTestSupport.harvest(registry, "planned-availability");
    }

    @Test
    public void test_if_get_all_planned_availabilities_returns_valid_response() {
        PlannedAvailabilities plannedAvailabilities =
                restClient.get().uri("/eu/planned-availabilities").retrieve().body(PlannedAvailabilities.class);
        PlannedAvailability first = plannedAvailabilities.getPlannedAvailabilities().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/planned-availability/AVAILABLE", first.getUri());
        assertEquals("AVAILABLE", first.getCode());
        assertEquals("available", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_planned_availability_by_code_returns_valid_response() {
        PlannedAvailability plannedAvailability =
                restClient.get().uri("/eu/planned-availabilities/TEMPORARY").retrieve().body(PlannedAvailability.class);

        assertNotNull(plannedAvailability);
        assertEquals("http://publications.europa.eu/resource/authority/planned-availability/TEMPORARY", plannedAvailability.getUri());
        assertEquals("TEMPORARY", plannedAvailability.getCode());
        assertEquals("midlertidig", plannedAvailability.getLabel().get(Language.NORWEGIAN_NYNORSK.code()));
    }

    @Test
    public void test_planned_availabilities_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/planned-availabilities", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(PlannedAvailabilityControllerIntegrationTest.class.getClassLoader().getResource("planned-availability-sparql-result.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
