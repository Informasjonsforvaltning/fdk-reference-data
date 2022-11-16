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
class ADMSStatusQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private GraphQLTestTemplate template;

    @Test
    void test_if_statuses_query_returns_valid_response() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/adms-statuses.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://purl.org/adms/status/Deprecated", response.get("$['data']['statuses'][1]['uri']"));
        assertEquals("Deprecated", response.get("$['data']['statuses'][1]['code']"));
        assertEquals("Deprecated", response.get("$['data']['statuses'][1]['label']['en']"));
    }

    @Test
    void test_if_publisher_type_by_code_query_returns_valid_response() throws IOException {
        GraphQLResponse response = template.perform("graphql/adms-status-by-code.graphql",
                mapper.valueToTree(Map.of("code", "Withdrawn")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://purl.org/adms/status/Withdrawn", response.get("$['data']['statusByCode']['uri']"));
        assertEquals("Withdrawn", response.get("$['data']['statusByCode']['code']"));
        assertEquals("Withdrawn", response.get("$['data']['statusByCode']['label']['en']"));
    }

    @Test
    void test_if_publisher_type_by_code_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/adms-status-by-code.graphql",
                mapper.valueToTree(Map.of("code", "INVALID")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['statusByCode']"));
    }
}
