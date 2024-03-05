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
class ReferenceTypeQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private GraphQLTestTemplate template;

    @Test
    void test_if_reference_type_query_returns_valid_response() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/reference-types.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("associativeRelation", response.get("$['data']['referenceTypes'][0]['code']"));
        assertEquals("Associated with", response.get("$['data']['referenceTypes'][0]['label']['en']"));
        assertEquals("Associated with", response.get("$['data']['referenceTypes'][0]['inverseLabel']['en']"));
    }

    @Test
    void test_if_reference_type_by_code_query_returns_valid_response() throws IOException {
        GraphQLResponse response = template.perform("graphql/reference-type-by-code.graphql",
                mapper.valueToTree(Map.of("code", "isRequiredBy")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("isRequiredBy", response.get("$['data']['referenceTypeByCode']['code']"));
        assertEquals("Is required by", response.get("$['data']['referenceTypeByCode']['label']['en']"));
        assertEquals("Requires", response.get("$['data']['referenceTypeByCode']['inverseLabel']['en']"));
    }

    @Test
    void test_if_reference_type_by_code_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/reference-type-by-code.graphql",
                mapper.valueToTree(Map.of("code", "INVALID")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['referenceTypeByCode']"));
    }
}
