package no.fdk.referencedata.graphql;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.eu.accessright.AccessRight;
import no.fdk.referencedata.container.AbstractContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

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
class AccessRightQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        HarvestTestSupport.harvest(registry, "access-right");
    }

    @Test
    void test_if_access_rights_query_returns_all_access_rights() {
        GraphQlTester.EntityList<AccessRight> result = graphQlTester.documentName("access-rights")
                .execute()
                .path("$['data']['accessRights']")
                .entityList(AccessRight.class);
        AccessRight accessRight = result.get().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/access-right/CONFIDENTIAL", accessRight.getUri());
        assertEquals("CONFIDENTIAL", accessRight.getCode());
        assertEquals("confidential", accessRight.getLabel().get("en"));
    }

    @Test
    void test_if_access_right_by_code_public_query_returns_public_access_right() {
        AccessRight result = graphQlTester.documentName("access-right-by-code")
                .variable("code", "PUBLIC")
                .execute()
                .path("$['data']['accessRightByCode']")
                .entity(AccessRight.class)
                .get();

        assertEquals("http://publications.europa.eu/resource/authority/access-right/PUBLIC", result.getUri());
        assertEquals("PUBLIC", result.getCode());
        assertEquals("public", result.getLabel().get("en"));
    }

}
