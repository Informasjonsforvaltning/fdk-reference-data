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
class ProvenanceQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private GraphQLTestTemplate template;

    @Test
    void test_if_provenance_query_returns_valid_response() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/provenance-statements.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://data.brreg.no/datakatalog/provinens/bruker", response.get("$['data']['provenanceStatements'][0]['uri']"));
        assertEquals("BRUKER", response.get("$['data']['provenanceStatements'][0]['code']"));
        assertEquals("User collection", response.get("$['data']['provenanceStatements'][0]['label']['en']"));
    }

    @Test
    void test_if_provenance_by_code_query_returns_valid_response() throws IOException {
        GraphQLResponse response = template.perform("graphql/provenance-statement-by-code.graphql",
                mapper.valueToTree(Map.of("code", "NASJONAL")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://data.brreg.no/datakatalog/provinens/nasjonal", response.get("$['data']['provenanceStatementByCode']['uri']"));
        assertEquals("NASJONAL", response.get("$['data']['provenanceStatementByCode']['code']"));
        assertEquals("Authoritativ source", response.get("$['data']['provenanceStatementByCode']['label']['en']"));
    }

    @Test
    void test_if_provenance_by_code_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/provenance-statement-by-code.graphql",
                mapper.valueToTree(Map.of("code", "INVALID")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['provenanceStatementByCode']"));
    }
}
