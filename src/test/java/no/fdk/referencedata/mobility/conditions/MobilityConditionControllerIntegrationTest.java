package no.fdk.referencedata.mobility.conditions;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class MobilityConditionControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MobilityConditionRepository mobilityConditionRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        MobilityConditionService mobilityConditionService = new MobilityConditionService(
                LocalHarvesters.mobilityCondition(),
                mobilityConditionRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        mobilityConditionService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_mobility_conditions_returns_valid_response() {
        MobilityConditions mobilityConditions =
                restClient.get()
                        .uri("/mobility/conditions-for-access-and-usage")
                        .retrieve()
                        .body(MobilityConditions.class);
        MobilityCondition first = mobilityConditions.getMobilityConditions().get(0);
        assertEquals("https://w3id.org/mobilitydcat-ap/conditions-for-access-and-usage/contractual-arrangement", first.getUri());
        assertEquals("contractual-arrangement", first.getCode());
        assertEquals("Contractual arrangement", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_mobility_condition_by_code_returns_valid_response() {
        MobilityCondition condition =
                restClient.get()
                        .uri("/mobility/conditions-for-access-and-usage/other")
                        .retrieve()
                        .body(MobilityCondition.class);

        assertNotNull(condition);
        assertEquals("https://w3id.org/mobilitydcat-ap/conditions-for-access-and-usage/other", condition.getUri());
        assertEquals("other", condition.getCode());
        assertEquals("Other", condition.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_mobility_conditions_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/mobility/conditions-for-access-and-usage", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(MobilityConditionControllerIntegrationTest.class.getClassLoader().getResource("mobility-conditions.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
