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
class ApiStatusQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private GraphQLTestTemplate template;

    @Test
    void test_if_api_statuses_query_returns_valid_response() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/api-statuses.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://fellesdatakatalog.brreg.no/reference-data/codes/apistastus/deprecated", response.get("$['data']['apiStatuses'][0]['uri']"));
        assertEquals("REMOVED", response.get("$['data']['apiStatuses'][0]['code']"));
        assertEquals("Removed", response.get("$['data']['apiStatuses'][0]['label']['en']"));
    }

    @Test
    void test_if_api_status_by_code_query_returns_valid_response() throws IOException {
        GraphQLResponse response = template.perform("graphql/api-status-by-code.graphql",
                mapper.valueToTree(Map.of("code", "EXPERIMENTAL")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://fellesdatakatalog.brreg.no/reference-data/codes/apistastus/nonproduction", response.get("$['data']['apiStatusByCode']['uri']"));
        assertEquals("EXPERIMENTAL", response.get("$['data']['apiStatusByCode']['code']"));
        assertEquals("Experimental", response.get("$['data']['apiStatusByCode']['label']['en']"));
    }

    @Test
    void test_if_api_status_by_code_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/api-status-by-code.graphql",
                mapper.valueToTree(Map.of("code", "INVALID")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['apiStatusByCode']"));
    }
}
