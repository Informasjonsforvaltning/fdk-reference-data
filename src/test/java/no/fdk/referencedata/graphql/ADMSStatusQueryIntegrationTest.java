package no.fdk.referencedata.graphql;

import no.fdk.referencedata.adms.status.ADMSStatus;
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
class ADMSStatusQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void test_if_statuses_query_returns_valid_response() {
        List<ADMSStatus> result = graphQlTester.documentName("adms-statuses")
                .execute()
                .path("$['data']['statuses']")
                .entityList(ADMSStatus.class)
                .get();

        assertEquals(4, result.size());

        ADMSStatus status = result.get(1);

        assertEquals("http://purl.org/adms/status/Deprecated", status.getUri());
        assertEquals("Deprecated", status.getCode());
        assertEquals("Deprecated", status.getLabel().get("en"));
        assertEquals("Frarådet", status.getLabel().get("nb"));
        assertEquals("Frarådd", status.getLabel().get("nn"));
    }

    @Test
    void test_if_publisher_type_by_code_query_returns_valid_response() {
        ADMSStatus result = graphQlTester.documentName("adms-status-by-code")
                .variable("code", "Withdrawn")
                .execute()
                .path("$['data']['statusByCode']")
                .entity(ADMSStatus.class)
                .get();

        assertEquals("http://purl.org/adms/status/Withdrawn", result.getUri());
        assertEquals("Withdrawn", result.getCode());
        assertEquals("Withdrawn", result.getLabel().get("en"));
        assertEquals("Trukket tilbake", result.getLabel().get("nb"));
        assertEquals("Trekt tilbake", result.getLabel().get("nn"));
    }

    @Test
    void test_if_publisher_type_by_code_query_returns_null() {
        graphQlTester.documentName("adms-status-by-code")
                .variable("code", "INVALID")
                .execute()
                .path("$['data']['statusByCode']")
                .valueIsNull();
    }

}
