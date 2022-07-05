package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.jayway.jsonpath.PathNotFoundException;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@ActiveProfiles("test")
class ApiSpecificationQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private GraphQLTestTemplate template;

    @Test
    void test_if_api_specification_query_returns_valid_response() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/api-specifications.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.norge.no/reference-data/api-specifications/account", response.get("$['data']['apiSpecifications'][0]['uri']"));
        assertEquals("account", response.get("$['data']['apiSpecifications'][0]['code']"));
        assertEquals("https://bitsnorge.github.io/dsop-accounts-api", response.get("$['data']['apiSpecifications'][0]['source']"));
        assertEquals("Account details", response.get("$['data']['apiSpecifications'][0]['label']['en']"));
    }

    @Test
    void test_if_api_specification_by_code_query_returns_valid_response() throws IOException {
        GraphQLResponse response = template.perform("graphql/api-specification-by-code.graphql",
                mapper.valueToTree(Map.of("code", "customer-relationship")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.norge.no/reference-data/api-specifications/customer-relationship", response.get("$['data']['apiSpecificationByCode']['uri']"));
        assertEquals("customer-relationship", response.get("$['data']['apiSpecificationByCode']['code']"));
        assertEquals("https://bitsnorge.github.io/dsop-kfr-api", response.get("$['data']['apiSpecificationByCode']['source']"));
        assertEquals("Customer relationship", response.get("$['data']['apiSpecificationByCode']['label']['en']"));
    }

    @Test
    void test_if_api_specification_by_code_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/api-specification-by-code.graphql",
                mapper.valueToTree(Map.of("code", "INVALID")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['apiSpecificationByCode']"));
    }
}
