package no.fdk.referencedata.graphql;

import no.fdk.referencedata.apistatus.ApiStatus;
import no.fdk.referencedata.container.AbstractContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@ActiveProfiles("test")
class ApiStatusQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void test_if_api_statuses_query_returns_valid_response() {
        List<ApiStatus> result = graphQlTester.documentName("api-statuses")
                .execute()
                .path("$['data']['apiStatuses']")
                .entityList(ApiStatus.class)
                .get();

        assertEquals(4, result.size());

        ApiStatus apiStatus = result.get(0);

        assertEquals("http://fellesdatakatalog.brreg.no/reference-data/codes/apistastus/deprecated", apiStatus.getUri());
        assertEquals("REMOVED", apiStatus.getCode());
        assertEquals("Avviklet", apiStatus.getLabel().get("nb"));
        assertEquals("Avvikla", apiStatus.getLabel().get("nn"));
        assertEquals("Removed", apiStatus.getLabel().get("en"));
    }

    @Test
    void test_if_api_status_by_code_query_returns_valid_response() {
        ApiStatus result = graphQlTester.documentName("api-status-by-code")
                .variable("code", "EXPERIMENTAL")
                .execute()
                .path("$['data']['apiStatusByCode']")
                .entity(ApiStatus.class)
                .get();

        assertEquals("http://fellesdatakatalog.brreg.no/reference-data/codes/apistastus/nonproduction", result.getUri());
        assertEquals("EXPERIMENTAL", result.getCode());
        assertEquals("Under utprøving", result.getLabel().get("nb"));
        assertEquals("Under utprøving", result.getLabel().get("nn"));
        assertEquals("Experimental", result.getLabel().get("en"));
    }

    @Test
    void test_if_api_status_by_code_query_returns_null() {
        graphQlTester.documentName("api-status-by-code")
                .variable("code", "INVALID")
                .execute()
                .path("$['data']['apiStatusByCode']")
                .valueIsNull();
    }
}
