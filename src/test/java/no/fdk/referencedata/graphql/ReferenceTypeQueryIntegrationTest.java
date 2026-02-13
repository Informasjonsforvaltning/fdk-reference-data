package no.fdk.referencedata.graphql;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.referencetypes.ReferenceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
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
@AutoConfigureGraphQlTester
@ActiveProfiles("test")
class ReferenceTypeQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void test_if_reference_type_query_returns_valid_response() {
        List<ReferenceType> result = graphQlTester.documentName("reference-types")
                .execute()
                .path("$['data']['referenceTypes']")
                .entityList(ReferenceType.class)
                .get();

        assertEquals(27, result.size());

        ReferenceType referenceType = result.get(0);

        assertEquals("associativeRelation", referenceType.getCode());
        assertEquals("Associated with", referenceType.getLabel().get("en"));
        assertEquals("Associated with", referenceType.getLabel().get("en"));
    }

    @Test
    void test_if_reference_type_by_code_query_returns_valid_response() {
        ReferenceType result = graphQlTester.documentName("reference-type-by-code")
                .variable("code", "isRequiredBy")
                .execute()
                .path("$['data']['referenceTypeByCode']")
                .entity(ReferenceType.class)
                .get();

        assertEquals("isRequiredBy", result.getCode());
        assertEquals("Is required by", result.getLabel().get("en"));
        assertEquals("Requires", result.getInverseLabel().get("en"));
    }

    @Test
    void test_if_reference_type_by_code_query_returns_null() {
        graphQlTester.documentName("reference-type-by-code")
                .variable("code", "INVALID")
                .execute()
                .path("$['data']['referenceTypeByCode']")
                .valueIsNull();
    }
}
