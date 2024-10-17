package no.fdk.referencedata.graphql;

import no.fdk.referencedata.ApplicationSettings;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubject;
import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubjectRepository;
import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubjectService;
import no.fdk.referencedata.digdir.conceptsubjects.LocalConceptSubjectHarvester;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

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
class ConceptSubjectQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ConceptSubjectRepository conceptSubjectRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        ConceptSubjectService conceptSubjectService = new ConceptSubjectService(
                new LocalConceptSubjectHarvester(new ApplicationSettings()),
                rdfSourceRepository,
                conceptSubjectRepository);

        conceptSubjectService.harvestAndSave();
    }

    @Test
    void test_if_concept_subjects_query_returns_all_concept_subjects() {
        List<ConceptSubject> result = graphQlTester.documentName("concept-subjects")
                .execute()
                .path("$['data']['conceptSubjects']")
                .entityList(ConceptSubject.class)
                .get();

        assertEquals(4, result.size());

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
