package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.FylkeRepository;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.FylkeService;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.LocalFylkeHarvester;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneRepository;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneService;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.LocalKommuneHarvester;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.search.SearchHit;
import no.fdk.referencedata.search.SearchRequest;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static no.fdk.referencedata.search.SearchAlternative.ADMINISTRATIVE_ENHETER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class SearchQueryIntegrationTest extends AbstractContainerTest {

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
    private GraphQlTester graphQlTester;

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
    void test_if_search_query_returns_geonorge_nasjon_hit() {
        SearchRequest req = SearchRequest.builder().query("norg").types(List.of(ADMINISTRATIVE_ENHETER)).build();
        List<SearchHit> result = graphQlTester.documentName("search")
                .variable("req", req)
                .execute()
                .path("$['data']['search']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(1, result.size());

        SearchHit hit = result.get(0);

        assertEquals("https://data.geonorge.no/administrativeEnheter/nasjon/id/173163", hit.getUri());
        assertEquals("173163", hit.getCode());
        assertEquals("Norge", hit.getLabel().get("nb"));
        assertEquals(ADMINISTRATIVE_ENHETER, hit.getType());
    }

    @Test
    void test_if_search_query_returns_geonorge_fylke_hit() {
        SearchRequest req = SearchRequest.builder().query("FYLKE 4").types(List.of(ADMINISTRATIVE_ENHETER)).build();
        List<SearchHit> result = graphQlTester.documentName("search")
                .variable("req", req)
                .execute()
                .path("$['data']['search']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(1, result.size());

        SearchHit hit = result.get(0);

        assertEquals("https://data.geonorge.no/administrativeEnheter/fylke/id/423456", hit.getUri());
        assertEquals("423456", hit.getCode());
        assertEquals("Fylke 4", hit.getLabel().get("nb"));
        assertEquals(ADMINISTRATIVE_ENHETER, hit.getType());
    }

    @Test
    void test_if_search_query_returns_geonorge_kommune_hit() {
        SearchRequest req = SearchRequest.builder().query("kommune 4").types(List.of(ADMINISTRATIVE_ENHETER)).build();
        List<SearchHit> result = graphQlTester.documentName("search")
                .variable("req", req)
                .execute()
                .path("$['data']['search']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(1, result.size());

        SearchHit hit = result.get(0);

        assertEquals("https://data.geonorge.no/administrativeEnheter/kommune/id/423456", hit.getUri());
        assertEquals("423456", hit.getCode());
        assertEquals("Kommune 4 norsk", hit.getLabel().get("nb"));
        assertEquals(ADMINISTRATIVE_ENHETER, hit.getType());
    }

    @Test
    void test_if_search_query_returns_cobined_fylke_and_kommune_hits() {
        SearchRequest req = SearchRequest.builder().query("e 2").types(List.of(ADMINISTRATIVE_ENHETER, ADMINISTRATIVE_ENHETER)).build();
        List<SearchHit> result = graphQlTester.documentName("search")
                .variable("req", req)
                .execute()
                .path("$['data']['search']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(2, result.size());

        assertEquals("https://data.geonorge.no/administrativeEnheter/fylke/id/223456", result.get(0).getUri());
        assertEquals("223456", result.get(0).getCode());
        assertEquals("Fylke 2", result.get(0).getLabel().get("nb"));
        assertEquals(ADMINISTRATIVE_ENHETER, result.get(0).getType());

        assertEquals("https://data.geonorge.no/administrativeEnheter/kommune/id/223456", result.get(1).getUri());
        assertEquals("223456", result.get(1).getCode());
        assertEquals("Kommune 2 norsk", result.get(1).getLabel().get("nb"));
        assertEquals(ADMINISTRATIVE_ENHETER, result.get(1).getType());
    }
}
