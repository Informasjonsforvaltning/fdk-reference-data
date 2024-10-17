package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.eu.accessright.AccessRight;
import no.fdk.referencedata.eu.accessright.AccessRightRepository;
import no.fdk.referencedata.eu.accessright.AccessRightService;
import no.fdk.referencedata.eu.accessright.LocalAccessRightHarvester;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
                "wiremock.host=dummy",
                "wiremock.port=0"
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class AccessRightQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private AccessRightRepository accessRightRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        AccessRightService accessRightService = new AccessRightService(
                new LocalAccessRightHarvester("1"),
                accessRightRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        accessRightService.harvestAndSave(false);
    }

    @Test
    void test_if_access_rights_query_returns_all_access_rights() {
        GraphQlTester.EntityList<AccessRight> result = graphQlTester.documentName("access-rights")
                .execute()
                .path("$['data']['accessRights']")
                .entityList(AccessRight.class);

        result.hasSize(6);

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

    @Test
    void test_if_access_right_by_code_unknown_query_returns_null() {
        graphQlTester.documentName("access-right-by-code")
                .variable("code", "unknown")
                .execute()
                .path("$['data']['accessRightByCode']")
                .valueIsNull();
    }

}
