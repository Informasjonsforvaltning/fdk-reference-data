package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.jayway.jsonpath.PathNotFoundException;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.FylkeRepository;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.FylkeService;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.LocalFylkeHarvester;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneRepository;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneService;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.LocalKommuneHarvester;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class FylkeQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @Autowired
    private FylkeRepository fylkeRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private GraphQLTestTemplate template;

    @BeforeEach
    public void setup() {
        FylkeService fylkeService = new FylkeService(
                new LocalFylkeHarvester(wiremockHost, wiremockPort),
                fylkeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        fylkeService.harvestAndSave();
    }

    @Test
    void test_if_fylke_query_returns_all_fylker() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/fylker.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.geonorge.no/administrativeEnheter/fylke/id/123456", response.get("$['data']['fylker'][0]['uri']"));
        assertEquals("Fylke 1", response.get("$['data']['fylker'][0]['fylkesnavn']"));
        assertEquals("123456", response.get("$['data']['fylker'][0]['fylkesnummer']"));
        assertNotNull(response.get("$['data']['fylker'][3]['fylkesnavn']"));
        assertThrows(PathNotFoundException.class, () -> response.get("$['data']['fylker'][4]"));
    }

    @Test
    void test_if_fylke_by_fylkesnummer_query_returns_norge() throws IOException {
        GraphQLResponse response = template.perform("graphql/fylke-by-fylkesnummer.graphql",
                mapper.valueToTree(Map.of("fylkesnummer", "223456")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.geonorge.no/administrativeEnheter/fylke/id/223456", response.get("$['data']['fylkeByFylkesnummer']['uri']"));
        assertThrows(PathNotFoundException.class, () -> response.get("$['data']['fylkeByFylkesnummer']['fylkesnavn']"));
    }

    @Test
    void test_if_fylke_by_fylkesnummer_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/fylke-by-fylkesnummer.graphql",
                mapper.valueToTree(Map.of("fylkesnummer", "11111")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['fylkeByFylkesnummer']"));
    }
}
