package no.fdk.referencedata.graphql;

import no.fdk.referencedata.apispecification.ApiSpecification;
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
class ApiSpecificationQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void test_if_api_specification_query_returns_valid_response() {
        List<ApiSpecification> result = graphQlTester.documentName("api-specifications")
                .execute()
                .path("$['data']['apiSpecifications']")
                .entityList(ApiSpecification.class)
                .get();

        assertEquals(2, result.size());

        ApiSpecification apiSpecification = result.get(0);

        assertEquals("https://data.norge.no/reference-data/api-specifications/account", apiSpecification.getUri());
        assertEquals("account", apiSpecification.getCode());
        assertEquals("https://bitsnorge.github.io/dsop-accounts-api", apiSpecification.getSource());
        assertEquals("Kontoopplysninger", apiSpecification.getLabel().get("nb"));
        assertEquals("Kontoopplysingar", apiSpecification.getLabel().get("nn"));
        assertEquals("Account details", apiSpecification.getLabel().get("en"));
    }

    @Test
    void test_if_api_specification_by_code_query_returns_valid_response() {
        ApiSpecification result = graphQlTester.documentName("api-specification-by-code")
                .variable("code", "customer-relationship")
                .execute()
                .path("$['data']['apiSpecificationByCode']")
                .entity(ApiSpecification.class)
                .get();

        assertEquals("https://data.norge.no/reference-data/api-specifications/customer-relationship", result.getUri());
        assertEquals("customer-relationship", result.getCode());
        assertEquals("https://bitsnorge.github.io/dsop-kfr-api", result.getSource());
        assertEquals("Kundeforhold", result.getLabel().get("nb"));
        assertEquals("Kundeforhold", result.getLabel().get("nn"));
        assertEquals("Customer relationship", result.getLabel().get("en"));
    }

    @Test
    void test_if_api_specification_by_code_query_returns_null() {
        graphQlTester.documentName("api-specification-by-code")
                .variable("code", "INVALID")
                .execute()
                .path("$['data']['apiSpecificationByCode']")
                .valueIsNull();
    }
}
