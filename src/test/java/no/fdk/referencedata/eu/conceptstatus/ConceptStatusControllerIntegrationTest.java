package no.fdk.referencedata.eu.conceptstatus;

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
import org.springframework.web.client.RestClient;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key"
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class ConceptStatusControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ConceptStatusRepository conceptStatusRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        ConceptStatusService conceptStatusService = new ConceptStatusService(
                LocalHarvesters.conceptStatus(),
                conceptStatusRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        conceptStatusService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_statuses_returns_valid_response() {
        ConceptStatuses statuses =
                restClient.get().uri("/eu/concept-statuses").retrieve().body(ConceptStatuses.class);
        ConceptStatus first = statuses.getConceptStatuses().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/concept-status/CANDIDATE", first.getUri());
        assertEquals("CANDIDATE", first.getCode());
        assertEquals("candidate", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_status_by_code_returns_valid_response() {
        ConceptStatus status =
                restClient.get().uri("/eu/concept-statuses/CURRENT").retrieve().body(ConceptStatus.class);

        assertNotNull(status);
        assertEquals("http://publications.europa.eu/resource/authority/concept-status/CURRENT", status.getUri());
        assertEquals("CURRENT", status.getCode());
        assertEquals("current", status.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_concept_status_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/concept-statuses", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(ConceptStatusControllerIntegrationTest.class.getClassLoader().getResource("concept-status.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
