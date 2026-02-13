package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.digdir.roletype.LocalRoleTypeHarvester;
import no.fdk.referencedata.digdir.roletype.RoleType;
import no.fdk.referencedata.digdir.roletype.RoleTypeRepository;
import no.fdk.referencedata.digdir.roletype.RoleTypeService;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
                "wiremock.host=dummy",
                "wiremock.port=0"
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class RoleTypeQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private RoleTypeRepository roleTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        RoleTypeService roleTypeService = new RoleTypeService(
                new LocalRoleTypeHarvester("1"),
                roleTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        roleTypeService.harvestAndSave(false);
    }

    @Test
    void test_if_access_rights_query_returns_all_access_rights() {
        List<RoleType> result = graphQlTester.documentName("role-types")
                .execute()
                .path("$['data']['roleTypes']")
                .entityList(RoleType.class)
                .get();

        assertEquals(5, result.size());

        RoleType roleType = result.get(0);

        assertEquals("https://data.norge.no/vocabulary/role-type#data-consumer", roleType.getUri());
        assertEquals("data-consumer", roleType.getCode());
        assertEquals("datakonsument", roleType.getLabel().get("nb"));
        assertEquals("data consumer", roleType.getLabel().get("en"));
    }

    @Test
    void test_if_access_right_by_code_public_query_returns_public_access_right() {
        RoleType result = graphQlTester.documentName("role-type-by-code")
                .variable("code", "service-provider")
                .execute()
                .path("$['data']['roleTypeByCode']")
                .entity(RoleType.class)
                .get();

        assertEquals("https://data.norge.no/vocabulary/role-type#service-provider", result.getUri());
        assertEquals("service-provider", result.getCode());
        assertEquals("tjenestetilbyder", result.getLabel().get("nb"));
        assertEquals("service provider", result.getLabel().get("en"));
    }

    @Test
    void test_if_access_right_by_code_unknown_query_returns_null() {
        graphQlTester.documentName("role-type-by-code")
                .variable("code", "unknown")
                .execute()
                .path("$['data']['roleTypeByCode']")
                .valueIsNull();
    }

}
