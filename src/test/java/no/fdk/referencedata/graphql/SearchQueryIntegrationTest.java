package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.geonorge.administrativeenheter.EnhetRepository;
import no.fdk.referencedata.geonorge.administrativeenheter.EnhetService;
import no.fdk.referencedata.geonorge.administrativeenheter.EnhetVariantRepository;
import no.fdk.referencedata.geonorge.administrativeenheter.LocalEnhetHarvester;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.search.SearchHit;
import no.fdk.referencedata.search.SearchRequest;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private EnhetRepository enhetRepository;

    @Autowired
    private EnhetVariantRepository enhetVariantRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        EnhetService enhetService = new EnhetService(
                new LocalEnhetHarvester(),
                enhetRepository,
                enhetVariantRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        enhetService.harvestAndSave();
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
        SearchRequest req = SearchRequest.builder().query("ROGAL").types(List.of(ADMINISTRATIVE_ENHETER)).build();
        List<SearchHit> result = graphQlTester.documentName("search")
                .variable("req", req)
                .execute()
                .path("$['data']['search']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(1, result.size());

        SearchHit hit = result.get(0);

        assertEquals("https://data.geonorge.no/administrativeEnheter/fylke/id/173152", hit.getUri());
        assertEquals("173152", hit.getCode());
        assertEquals("Rogaland", hit.getLabel().get("nb"));
        assertEquals(ADMINISTRATIVE_ENHETER, hit.getType());
    }

    @Test
    void test_if_search_query_returns_geonorge_kommune_hit() {
        SearchRequest req = SearchRequest.builder().query("bambl").types(List.of(ADMINISTRATIVE_ENHETER)).build();
        List<SearchHit> result = graphQlTester.documentName("search")
                .variable("req", req)
                .execute()
                .path("$['data']['search']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(1, result.size());

        SearchHit hit = result.get(0);

        assertEquals("https://data.geonorge.no/administrativeEnheter/kommune/id/172729", hit.getUri());
        assertEquals("172729", hit.getCode());
        assertEquals("Bamble", hit.getLabel().get("nb"));
        assertEquals(ADMINISTRATIVE_ENHETER, hit.getType());
    }

    @Test
    void test_if_search_query_returns_combined_fylke_and_nasjon_hits() {
        SearchRequest req = SearchRequest.builder().query("nor").types(List.of(ADMINISTRATIVE_ENHETER)).build();
        List<SearchHit> result = graphQlTester.documentName("search")
                .variable("req", req)
                .execute()
                .path("$['data']['search']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(2, result.size());

        assertEquals("https://data.geonorge.no/administrativeEnheter/fylke/id/173144", result.get(0).getUri());
        assertEquals("173144", result.get(0).getCode());
        assertEquals("Nordland", result.get(0).getLabel().get("nb"));
        assertEquals(ADMINISTRATIVE_ENHETER, result.get(0).getType());

        assertEquals("https://data.geonorge.no/administrativeEnheter/nasjon/id/173163", result.get(1).getUri());
        assertEquals("173163", result.get(1).getCode());
        assertEquals("Norge", result.get(1).getLabel().get("nb"));
        assertEquals(ADMINISTRATIVE_ENHETER, result.get(1).getType());
    }

    @Test
    void test_if_that_hits_that_starts_with_search_query_is_prioritized_in_sort() {
        SearchRequest req = SearchRequest.builder().query("la").types(List.of(ADMINISTRATIVE_ENHETER)).build();
        List<SearchHit> result = graphQlTester.documentName("search")
                .variable("req", req)
                .execute()
                .path("$['data']['search']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(6, result.size());

        assertEquals("https://data.geonorge.no/administrativeEnheter/kommune/id/172987172681", result.get(0).getUri());
        assertEquals("172987172681", result.get(0).getCode());
        assertEquals("Larvik", result.get(0).getLabel().get("nb"));
        assertEquals(ADMINISTRATIVE_ENHETER, result.get(0).getType());

        assertEquals("https://data.geonorge.no/administrativeEnheter/fylke/id/173154173155", result.get(1).getUri());
        assertEquals("173154173155", result.get(1).getCode());
        assertEquals("Innlandet", result.get(1).getLabel().get("nb"));
        assertEquals(ADMINISTRATIVE_ENHETER, result.get(1).getType());

        assertEquals("https://data.geonorge.no/administrativeEnheter/fylke/id/173144", result.get(2).getUri());
        assertEquals("173144", result.get(2).getCode());
        assertEquals("Nordland", result.get(2).getLabel().get("nb"));
        assertEquals(ADMINISTRATIVE_ENHETER, result.get(2).getType());
    }
}
