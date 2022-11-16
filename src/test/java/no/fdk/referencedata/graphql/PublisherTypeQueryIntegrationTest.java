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
class PublisherTypeQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private GraphQLTestTemplate template;

    @Test
    void test_if_publisher_type_query_returns_valid_response() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/publisher-types.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://purl.org/adms/publishertype/Company", response.get("$['data']['publisherTypes'][1]['uri']"));
        assertEquals("Company", response.get("$['data']['publisherTypes'][1]['code']"));
        assertEquals("Company", response.get("$['data']['publisherTypes'][1]['label']['en']"));
    }

    @Test
    void test_if_publisher_type_by_code_query_returns_valid_response() throws IOException {
        GraphQLResponse response = template.perform("graphql/publisher-type-by-code.graphql",
                mapper.valueToTree(Map.of("code", "SupraNationalAuthority")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://purl.org/adms/publishertype/SupraNationalAuthority", response.get("$['data']['publisherTypeByCode']['uri']"));
        assertEquals("SupraNationalAuthority", response.get("$['data']['publisherTypeByCode']['code']"));
        assertEquals("Supra-national authority", response.get("$['data']['publisherTypeByCode']['label']['en']"));
    }

    @Test
    void test_if_publisher_type_by_code_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/publisher-type-by-code.graphql",
                mapper.valueToTree(Map.of("code", "INVALID")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['publisherTypeByCode']"));
    }
}
