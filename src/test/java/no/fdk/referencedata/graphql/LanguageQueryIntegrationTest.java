package no.fdk.referencedata.graphql;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.eu.language.Language;
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
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class LanguageQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        HarvestTestSupport.harvest(registry, "language");
    }

    @Test
    void test_if_languages_query_returns_all_languages() {
        List<Language> result = graphQlTester.documentName("languages")
                .execute()
                .path("$['data']['languages']")
                .entityList(Language.class)
                .get();
        Language language = result.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/language/ENG", language.getUri());
        assertEquals("ENG", language.getCode());
        assertEquals("English", language.getLabel().get("en"));
    }

    @Test
    void test_if_language_by_code_query_returns_correct_language() {
        Language result = graphQlTester.documentName("language-by-code")
                .variable("code", "NOB")
                .execute()
                .path("$['data']['languageByCode']")
                .entity(Language.class)
                .get();

        assertEquals("http://publications.europa.eu/resource/authority/language/NOB", result.getUri());
        assertEquals("NOB", result.getCode());
        assertEquals("Norwegian Bokmål", result.getLabel().get("en"));
        assertEquals("norsk (bokmål)", result.getLabel().get("no"));
    }

}
