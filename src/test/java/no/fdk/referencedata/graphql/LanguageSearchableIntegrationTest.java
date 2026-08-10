package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
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

import static no.fdk.referencedata.search.SearchAlternative.EU_LANGUAGES;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class LanguageSearchableIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        HarvestTestSupport.harvest(registry, "language");
    }

    @Test
    void test_if_search_query_returns_language_hit() {
        SearchRequest req = SearchRequest.builder().query("English").types(List.of(EU_LANGUAGES)).build();
        List<SearchHit> result = graphQlTester.documentName("search")
                .variable("req", objectMapper.convertValue(req, Map.class))
                .execute()
                .path("$['data']['search']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(1, result.size());

        SearchHit hit = result.get(0);

        assertEquals("http://publications.europa.eu/resource/authority/language/ENG", hit.getUri());
        assertEquals("ENG", hit.getCode());
        assertEquals("English", hit.getLabel().get("en"));
        assertEquals(EU_LANGUAGES, hit.getType());
    }

    @Test
    void test_if_that_hits_that_starts_with_search_query_is_prioritized_in_sort() {
        SearchRequest req = SearchRequest.builder().query("nor").types(List.of(EU_LANGUAGES)).build();
        List<SearchHit> result = graphQlTester.documentName("search")
                .variable("req", objectMapper.convertValue(req, Map.class))
                .execute()
                .path("$['data']['search']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(2, result.size());

        assertEquals("http://publications.europa.eu/resource/authority/language/NOR", result.get(0).getUri());
        assertEquals("NOR", result.get(0).getCode());
        assertEquals("Norwegian", result.get(0).getLabel().get("en"));
        assertEquals(EU_LANGUAGES, result.get(0).getType());
    }

    @Test
    void test_if_find_by_uris_query_returns_language_hits() {
        List<String> expectedURIs = List.of(
                "http://publications.europa.eu/resource/authority/language/ENG",
                "http://publications.europa.eu/resource/authority/language/NOB"
        );
        FindByURIsRequest req = FindByURIsRequest.builder().uris(expectedURIs).types(List.of(EU_LANGUAGES)).build();

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

        assertEquals(expectedURIs.stream().sorted().toList(), actualURIs);
    }
}
