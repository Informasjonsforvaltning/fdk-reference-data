package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import no.fdk.referencedata.container.AbstractContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@ActiveProfiles("test")
class LinguisticSystemQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private GraphQLTestTemplate template;

    @Test
    void test_if_linguistic_systems_query_returns_valid_response() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/linguistic-systems.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://publications.europa.eu/resource/authority/language/ENG", response.get("$['data']['linguisticSystems'][0]['uri']"));
        assertEquals("ENG", response.get("$['data']['linguisticSystems'][0]['code']"));
        assertEquals("English", response.get("$['data']['linguisticSystems'][0]['label']['en']"));
    }

    @Test
    void test_if_linguistic_system_by_code_query_returns_valid_response() throws IOException {
        GraphQLResponse response = template.perform("graphql/linguistic-system-by-code.graphql",
                mapper.valueToTree(Map.of("code", "NOB")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://publications.europa.eu/resource/authority/language/NOB", response.get("$['data']['linguisticSystemByCode']['uri']"));
        assertEquals("NOB", response.get("$['data']['linguisticSystemByCode']['code']"));
        assertEquals("Norwegian Bokmål", response.get("$['data']['linguisticSystemByCode']['label']['en']"));
    }

    @Test
    void test_if_linguistic_system_by_code_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/linguistic-system-by-code.graphql",
                mapper.valueToTree(Map.of("code", "INVALID")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['linguisticSystemByCode']"));
    }
}
