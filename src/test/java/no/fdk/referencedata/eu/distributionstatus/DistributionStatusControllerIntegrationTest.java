package no.fdk.referencedata.eu.distributionstatus;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static no.fdk.referencedata.eu.distributionstatus.LocalDistributionStatusHarvester.DISTRIBUTION_STATUS_SIZE;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class DistributionStatusControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DistributionStatusRepository distributionStatusRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        DistributionStatusService distributionStatusService = new DistributionStatusService(
                new LocalDistributionStatusHarvester(),
                distributionStatusRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        distributionStatusService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_distribution_statuses_returns_valid_response() {
        DistributionStatuses distributionStatuses =
                restClient.get().uri("/eu/distribution-statuses").retrieve().body(DistributionStatuses.class);

        assertEquals(DISTRIBUTION_STATUS_SIZE, distributionStatuses.getDistributionStatuses().size());

        DistributionStatus first = distributionStatuses.getDistributionStatuses().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/distribution-status/COMPLETED", first.getUri());
        assertEquals("COMPLETED", first.getCode());
        assertEquals("completed", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_distribution_status_by_code_returns_valid_response() {
        DistributionStatus distributionStatus =
                restClient.get().uri("/eu/distribution-statuses/DEVELOP").retrieve().body(DistributionStatus.class);

        assertNotNull(distributionStatus);
        assertEquals("http://publications.europa.eu/resource/authority/distribution-status/DEVELOP", distributionStatus.getUri());
        assertEquals("DEVELOP", distributionStatus.getCode());
        assertEquals("under utvikling", distributionStatus.getLabel().get(Language.NORWEGIAN_NYNORSK.code()));
    }

    @Test
    public void test_if_post_distribution_statuses_fails_without_api_key() {
        assertEquals(DISTRIBUTION_STATUS_SIZE, distributionStatusRepository.count());


        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = restClient.post().uri("/eu/distribution-statuses")
                .headers(h -> h.addAll(headers)).exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(DISTRIBUTION_STATUS_SIZE, distributionStatusRepository.count());

    }

    @Test
    public void test_if_post_distribution_statuses_executes_a_force_update() {
        assertEquals(DISTRIBUTION_STATUS_SIZE, distributionStatusRepository.count());


        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = restClient.post().uri("/eu/distribution-statuses")
                .headers(h -> h.addAll(headers)).exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(DISTRIBUTION_STATUS_SIZE, distributionStatusRepository.count());

    }

    @Test
    public void test_distribution_statuses_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/distribution-statuses", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(DistributionStatusControllerIntegrationTest.class.getClassLoader().getResource("distribution-status-sparql-result.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
