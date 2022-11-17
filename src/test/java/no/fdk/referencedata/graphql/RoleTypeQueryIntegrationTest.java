package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.digdir.roletype.LocalRoleTypeHarvester;
import no.fdk.referencedata.digdir.roletype.RoleTypeRepository;
import no.fdk.referencedata.digdir.roletype.RoleTypeService;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
                "wiremock.host=dummy",
                "wiremock.port=0"
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class RoleTypeQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private RoleTypeRepository roleTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private GraphQLTestTemplate template;

    @BeforeEach
    public void setup() {
        RoleTypeService roleTypeService = new RoleTypeService(
                new LocalRoleTypeHarvester("1"),
                roleTypeRepository,
                harvestSettingsRepository);

        roleTypeService.harvestAndSave(false);
    }

    @Test
    void test_if_access_rights_query_returns_all_access_rights() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/role-types.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.norge.no/vocabulary/role-type#data-consumer", response.get("$['data']['roleTypes'][0]['uri']"));
        assertEquals("data-consumer", response.get("$['data']['roleTypes'][0]['code']"));
        assertEquals("data consumer", response.get("$['data']['roleTypes'][0]['label']['en']"));
    }

    @Test
    void test_if_access_right_by_code_public_query_returns_public_access_right() throws IOException {
        GraphQLResponse response = template.perform("graphql/role-type-by-code.graphql",
                mapper.valueToTree(Map.of("code", "service-provider")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.norge.no/vocabulary/role-type#service-provider", response.get("$['data']['roleTypeByCode']['uri']"));
        assertEquals("service-provider", response.get("$['data']['roleTypeByCode']['code']"));
        assertEquals("service provider", response.get("$['data']['roleTypeByCode']['label']['en']"));
    }

    @Test
    void test_if_access_right_by_code_unknown_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/role-type-by-code.graphql",
                mapper.valueToTree(Map.of("code", "unknown")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['roleTypeByCode']"));
    }

}
