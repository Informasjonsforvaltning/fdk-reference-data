package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.geonames.GeonamesFylke;
import no.fdk.referencedata.geonames.GeonamesFylkeRepository;
import no.fdk.referencedata.geonames.GeonamesKommune;
import no.fdk.referencedata.geonames.GeonamesKommuneRepository;
import no.fdk.referencedata.search.FindByURIsRequest;
import no.fdk.referencedata.search.SearchHit;
import no.fdk.referencedata.search.SearchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static no.fdk.referencedata.search.SearchAlternative.GEONAMES;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class GeonamesSearchableIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GeonamesFylkeRepository geonamesFylkeRepository;

    @Autowired
    private GeonamesKommuneRepository geonamesKommuneRepository;

    @Autowired
    private GraphQlTester graphQlTester;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        geonamesKommuneRepository.deleteAll();
        geonamesFylkeRepository.deleteAll();
        geonamesFylkeRepository.saveAll(List.of(
                GeonamesFylke.builder()
                        .uri("https://sws.geonames.org/7626836/")
                        .geonameId("7626836")
                        .name("Agder")
                        .build(),
                GeonamesFylke.builder()
                        .uri("https://sws.geonames.org/3162656/")
                        .geonameId("3162656")
                        .name("Vestland")
                        .build()
        ));
        geonamesKommuneRepository.saveAll(List.of(
                GeonamesKommune.builder()
                        .uri("https://sws.geonames.org/3162994/")
                        .geonameId("3162994")
                        .name("Bergen")
                        .fylkeGeonameId("3162656")
                        .build(),
                GeonamesKommune.builder()
                        .uri("https://sws.geonames.org/3141558/")
                        .geonameId("3141558")
                        .name("Stavanger")
                        .fylkeGeonameId("607872")
                        .build()
        ));
    }

    @Test
    void test_if_search_query_returns_geonames_hit() {
        SearchRequest req = SearchRequest.builder().query("Agder").types(List.of(GEONAMES)).build();
        List<SearchHit> result = graphQlTester.documentName("search")
                .variable("req", objectMapper.convertValue(req, Map.class))
                .execute()
                .path("$['data']['search']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(1, result.size());

        SearchHit hit = result.get(0);

        assertEquals("https://sws.geonames.org/7626836/", hit.getUri());
        assertEquals("7626836", hit.getCode());
        assertEquals("Agder", hit.getLabel().get("no"));
        assertEquals(GEONAMES, hit.getType());
    }

    @Test
    void test_if_that_hits_that_starts_with_search_query_is_prioritized_in_sort() {
        SearchRequest req = SearchRequest.builder().query("st").types(List.of(GEONAMES)).build();
        List<SearchHit> result = graphQlTester.documentName("search")
                .variable("req", objectMapper.convertValue(req, Map.class))
                .execute()
                .path("$['data']['search']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(2, result.size());

        assertEquals("https://sws.geonames.org/3141558/", result.get(0).getUri());
        assertEquals("3141558", result.get(0).getCode());
        assertEquals("Stavanger", result.get(0).getLabel().get("no"));
        assertEquals(GEONAMES, result.get(0).getType());

        assertEquals("https://sws.geonames.org/3162656/", result.get(1).getUri());
        assertEquals("3162656", result.get(1).getCode());
        assertEquals("Vestland", result.get(1).getLabel().get("no"));
        assertEquals(GEONAMES, result.get(1).getType());
    }

    @Test
    void test_if_find_by_uris_query_returns_geonames_hits() {
        List<String> expectedURIs = List.of(
                "https://sws.geonames.org/7626836/",
                "https://sws.geonames.org/3162994/"
        );
        FindByURIsRequest req = FindByURIsRequest.builder().uris(expectedURIs).types(List.of(GEONAMES)).build();

        List<SearchHit> actual = graphQlTester.documentName("find-by-uris")
                .variable("req", objectMapper.convertValue(req, Map.class))
                .execute()
                .path("$['data']['findByURIs']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(2, actual.size());

        List<String> actualURIs = Stream.of(
                actual.get(0).getUri(),
                actual.get(1).getUri()
        ).sorted().toList();

        assertEquals(List.of(
                "https://sws.geonames.org/3162994/",
                "https://sws.geonames.org/7626836/"
        ), actualURIs);
    }
}
