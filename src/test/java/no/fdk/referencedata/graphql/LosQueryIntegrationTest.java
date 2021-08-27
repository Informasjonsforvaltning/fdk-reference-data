package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.jayway.jsonpath.PathNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
class LosQueryIntegrationTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private GraphQLTestTemplate template;

    @Test
    void test_if_los_themes_and_words_query_returns_all_themes_and_words_as_los_nodes() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/los-themes-and-words.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://psi.norge.no/los/ord/abort", response.get("$['data']['losThemesAndWords'][0]['uri']"));
        assertEquals("Abortion", response.get("$['data']['losThemesAndWords'][0]['name']['en']"));
    }

    @Test
    void test_if_los_themes_and_words_by_uris_query_returns_abortion_los_node() throws IOException {
        GraphQLResponse response = template.perform("graphql/los-themes-and-words-by-uris.graphql",
                mapper.valueToTree(Map.of("uris", List.of("https://psi.norge.no/los/ord/abort"))));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://psi.norge.no/los/ord/abort", response.get("$['data']['losThemesAndWords'][0]['uri']"));
        assertThrows(PathNotFoundException.class, () -> response.get("$['data']['losThemesAndWords'][0]['name']"));
    }

    @Test
    void test_if_los_themes_and_words_by_uris_unknown_query_returns_empty_list() throws IOException {
        GraphQLResponse response = template.perform("graphql/los-themes-and-words-by-uris.graphql",
                mapper.valueToTree(Map.of("uris", List.of("https://psi.norge.no/los/ord/unknown"))));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertThrows(PathNotFoundException.class, () -> response.get("$['data']['losThemesAndWords'][0]"));
    }
}
