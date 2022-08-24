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
class OpenLicenseQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private GraphQLTestTemplate template;

    @Test
    void test_if_open_license_query_returns_valid_response() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/open-licenses.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://publications.europa.eu/resource/authority/licence/CC_BY_4_0", response.get("$['data']['openLicenses'][1]['uri']"));
        assertEquals("CC BY 4.0", response.get("$['data']['openLicenses'][1]['code']"));
        assertNull(response.get("$['data']['openLicenses'][1]['isReplacedBy']"));
        assertEquals("Creative Commons Attribution 4.0 International", response.get("$['data']['openLicenses'][1]['label']['en']"));
    }

    @Test
    void test_if_linguistic_system_by_code_query_returns_valid_response() throws IOException {
        GraphQLResponse response = template.perform("graphql/open-license-by-code.graphql",
                mapper.valueToTree(Map.of("code", "NLOD20")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://publications.europa.eu/resource/authority/licence/NLOD_2_0", response.get("$['data']['openLicenseByCode']['uri']"));
        assertEquals("NLOD20", response.get("$['data']['openLicenseByCode']['code']"));
        assertEquals("Norwegian Licence for Open Government Data", response.get("$['data']['openLicenseByCode']['label']['en']"));
    }

    @Test
    void test_if_linguistic_system_by_code_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/open-license-by-code.graphql",
                mapper.valueToTree(Map.of("code", "INVALID")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['openLicenseByCode']"));
    }
}
