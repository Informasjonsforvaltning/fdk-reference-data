package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import no.fdk.referencedata.eu.accessright.AccessRightRepository;
import no.fdk.referencedata.eu.accessright.AccessRightService;
import no.fdk.referencedata.eu.accessright.LocalAccessRightHarvester;
import no.fdk.referencedata.mongo.AbstractMongoDbContainerTest;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
class AccessRightQueryIntegrationTest extends AbstractMongoDbContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private AccessRightRepository accessRightRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private GraphQLTestTemplate template;

    @BeforeEach
    public void setup() {
        AccessRightService accessRightService = new AccessRightService(
                new LocalAccessRightHarvester("1"),
                accessRightRepository,
                harvestSettingsRepository);

        accessRightService.harvestAndSave(false);
    }

    @Test
    void test_if_access_rights_query_returns_all_access_rights() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/access-rights.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://publications.europa.eu/resource/authority/access-right/CONFIDENTIAL", response.get("$['data']['accessRights'][0]['uri']"));
        assertEquals("CONFIDENTIAL", response.get("$['data']['accessRights'][0]['code']"));
        assertEquals("confidential", response.get("$['data']['accessRights'][0]['label']['en']"));
    }

    @Test
    void test_if_access_right_by_code_public_query_returns_public_access_right() throws IOException {
        GraphQLResponse response = template.perform("graphql/access-right-by-code.graphql",
                mapper.valueToTree(Map.of("code", "PUBLIC")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://publications.europa.eu/resource/authority/access-right/PUBLIC", response.get("$['data']['accessRightByCode']['uri']"));
        assertEquals("PUBLIC", response.get("$['data']['accessRightByCode']['code']"));
        assertEquals("public", response.get("$['data']['accessRightByCode']['label']['en']"));
    }

    @Test
    void test_if_access_right_by_code_unknown_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/access-right-by-code.graphql",
                mapper.valueToTree(Map.of("code", "unknown")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['accessRightByCode']"));
    }

}
