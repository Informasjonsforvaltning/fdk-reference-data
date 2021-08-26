package no.fdk.referencedata.graphql;

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

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
class AccessRightQueryIntegrationTest extends AbstractMongoDbContainerTest {

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

        accessRightService.harvestAndSave();
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
        GraphQLResponse response = template.postForResource("graphql/access-right-by-code.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://publications.europa.eu/resource/authority/access-right/PUBLIC", response.get("$['data']['accessRightByCode']['uri']"));
        assertEquals("PUBLIC", response.get("$['data']['accessRightByCode']['code']"));
        assertEquals("public", response.get("$['data']['accessRightByCode']['label']['en']"));
    }

    @Test
    void test_if_access_right_by_code_unknown_query_returns_null() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/access-right-by-code-unknown.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['accessRightByCode']"));
    }

}
