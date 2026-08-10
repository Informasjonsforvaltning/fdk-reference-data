package no.fdk.referencedata.graphql;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubject;
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
class ConceptSubjectQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        HarvestTestSupport.harvest(registry, "concept-subject");
    }

    @Test
    void test_if_concept_subjects_query_returns_all_concept_subjects() {
        List<ConceptSubject> result = graphQlTester.documentName("concept-subjects")
                .execute()
                .path("$['data']['conceptSubjects']")
                .entityList(ConceptSubject.class)
                .get();
        ConceptSubject conceptSubject = result.get(0);

        assertEquals(
                "https://catalog-admin-service.staging.fellesdatakatalog.digdir.no/123456789/concepts/subjects#1",
                conceptSubject.getUri()
        );
        assertEquals("1", conceptSubject.getCode());
        assertEquals("nb 1", conceptSubject.getLabel().get("nb"));
        assertEquals("nn 1", conceptSubject.getLabel().get("nn"));
        assertEquals("en 1", conceptSubject.getLabel().get("en"));
    }

}
