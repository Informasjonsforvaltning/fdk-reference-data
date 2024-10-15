package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.FylkeRepository;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.FylkeService;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.LocalFylkeHarvester;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneRepository;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneService;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.LocalKommuneHarvester;
import no.fdk.referencedata.geonorge.administrativeenheter.nasjon.NasjonService;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.search.SearchRequest;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static no.fdk.referencedata.search.SearchAlternative.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class SearchQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @Autowired
    private FylkeRepository fylkeRepository;

    @Autowired
    private KommuneRepository kommuneRepository;

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

        KommuneService kommuneService = new KommuneService(
                new LocalKommuneHarvester(wiremockHost, wiremockPort),
                kommuneRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        kommuneService.harvestAndSave();
    }

    @Test
    void test_if_search_query_returns_geonorge_nasjon_hit() throws IOException {
        SearchRequest req = SearchRequest.builder().query("norg").types(List.of(ADMINISTRATIVE_ENHETER)).build();
        GraphQLResponse response = template.perform("graphql/search.graphql", mapper.valueToTree(Map.of("req", req)));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.geonorge.no/administrativeEnheter/nasjon/id/173163", response.get("$['data']['search'][0]['uri']"));
        assertEquals("173163", response.get("$['data']['search'][0]['code']"));
        assertEquals("Norge", response.get("$['data']['search'][0]['label']['nb']"));
        assertEquals("ADMINISTRATIVE_ENHETER", response.get("$['data']['search'][0]['type']"));
    }

    @Test
    void test_if_search_query_returns_geonorge_fylke_hit() throws IOException {
        SearchRequest req = SearchRequest.builder().query("FYLKE 4").types(List.of(ADMINISTRATIVE_ENHETER)).build();
        GraphQLResponse response = template.perform("graphql/search.graphql", mapper.valueToTree(Map.of("req", req)));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.geonorge.no/administrativeEnheter/fylke/id/423456", response.get("$['data']['search'][0]['uri']"));
        assertEquals("423456", response.get("$['data']['search'][0]['code']"));
        assertEquals("Fylke 4", response.get("$['data']['search'][0]['label']['nb']"));
        assertEquals("ADMINISTRATIVE_ENHETER", response.get("$['data']['search'][0]['type']"));
    }

    @Test
    void test_if_search_query_returns_geonorge_kommune_hit() throws IOException {
        SearchRequest req = SearchRequest.builder().query("kommune 4").types(List.of(ADMINISTRATIVE_ENHETER)).build();
        GraphQLResponse response = template.perform("graphql/search.graphql", mapper.valueToTree(Map.of("req", req)));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.geonorge.no/administrativeEnheter/kommune/id/423456", response.get("$['data']['search'][0]['uri']"));
        assertEquals("423456", response.get("$['data']['search'][0]['code']"));
        assertEquals("Kommune 4 norsk", response.get("$['data']['search'][0]['label']['nb']"));
        assertEquals("ADMINISTRATIVE_ENHETER", response.get("$['data']['search'][0]['type']"));
    }

    @Test
    void test_if_search_query_returns_cobined_fylke_and_kommune_hits() throws IOException {
        SearchRequest req = SearchRequest.builder().query("e 2").types(List.of(ADMINISTRATIVE_ENHETER, ADMINISTRATIVE_ENHETER)).build();
        GraphQLResponse response = template.perform("graphql/search.graphql", mapper.valueToTree(Map.of("req", req)));
        assertNotNull(response);
        assertTrue(response.isOk());

        assertEquals("https://data.geonorge.no/administrativeEnheter/fylke/id/223456", response.get("$['data']['search'][0]['uri']"));
        assertEquals("223456", response.get("$['data']['search'][0]['code']"));
        assertEquals("Fylke 2", response.get("$['data']['search'][0]['label']['nb']"));
        assertEquals("ADMINISTRATIVE_ENHETER", response.get("$['data']['search'][0]['type']"));

        assertEquals("https://data.geonorge.no/administrativeEnheter/kommune/id/223456", response.get("$['data']['search'][1]['uri']"));
        assertEquals("223456", response.get("$['data']['search'][1]['code']"));
        assertEquals("Kommune 2 norsk", response.get("$['data']['search'][1]['label']['nb']"));
        assertEquals("ADMINISTRATIVE_ENHETER", response.get("$['data']['search'][1]['type']"));
    }
}
