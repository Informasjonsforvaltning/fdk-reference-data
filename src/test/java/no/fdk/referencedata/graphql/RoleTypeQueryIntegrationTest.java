package no.fdk.referencedata.graphql;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.digdir.roletype.RoleType;
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
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        HarvestTestSupport.harvest(registry, "role-type");
    }

    @Test
    void test_if_access_rights_query_returns_all_access_rights() {
        List<RoleType> result = graphQlTester.documentName("role-types")
                .execute()
                .path("$['data']['roleTypes']")
                .entityList(RoleType.class)
                .get();
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

}
