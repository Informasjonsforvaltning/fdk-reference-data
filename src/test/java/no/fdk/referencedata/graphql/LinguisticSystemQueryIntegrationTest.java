package no.fdk.referencedata.graphql;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.linguisticsystem.LinguisticSystem;
import org.junit.jupiter.api.Assertions;
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
class LinguisticSystemQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void test_if_linguistic_systems_query_returns_valid_response() {
        List<LinguisticSystem> result = graphQlTester.documentName("linguistic-systems")
                .execute()
                .path("$['data']['linguisticSystems']")
                .entityList(LinguisticSystem.class)
                .get();

        Assertions.assertEquals(5, result.size());

        LinguisticSystem linguisticSystem = result.get(0);

        assertEquals("http://publications.europa.eu/resource/authority/language/ENG", linguisticSystem.getUri());
        assertEquals("ENG", linguisticSystem.getCode());
        assertEquals("Engelsk", linguisticSystem.getLabel().get("no"));
        assertEquals("Engelsk", linguisticSystem.getLabel().get("nb"));
        assertEquals("Engelsk", linguisticSystem.getLabel().get("nn"));
        assertEquals("English", linguisticSystem.getLabel().get("en"));
    }

    @Test
    void test_if_linguistic_system_by_code_query_returns_valid_response() {
        LinguisticSystem result = graphQlTester.documentName("linguistic-system-by-code")
                .variable("code", "NOB")
                .execute()
                .path("$['data']['linguisticSystemByCode']")
                .entity(LinguisticSystem.class)
                .get();

        assertEquals("http://publications.europa.eu/resource/authority/language/NOB", result.getUri());
        assertEquals("NOB", result.getCode());
        assertEquals("Norsk Bokmål", result.getLabel().get("no"));
        assertEquals("Norsk Bokmål", result.getLabel().get("nb"));
        assertEquals("Norsk Bokmål", result.getLabel().get("nn"));
        assertEquals("Norwegian Bokmål", result.getLabel().get("en"));
    }

    @Test
    void test_if_linguistic_system_by_code_query_returns_null() {
        graphQlTester.documentName("linguistic-system-by-code")
                .variable("code", "INVALID")
                .execute()
                .path("$['data']['linguisticSystemByCode']")
                .valueIsNull();
    }
}
