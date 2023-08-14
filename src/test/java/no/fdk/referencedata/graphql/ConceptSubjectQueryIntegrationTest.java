package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import no.fdk.referencedata.ApplicationSettings;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubjectRepository;
import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubjectService;
import no.fdk.referencedata.digdir.conceptsubjects.LocalConceptSubjectHarvester;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ConceptSubjectRepository conceptSubjectRepository;

    @Autowired
    private GraphQLTestTemplate template;

    @BeforeEach
    public void setup() {
        ConceptSubjectService conceptSubjectService = new ConceptSubjectService(
                new LocalConceptSubjectHarvester(new ApplicationSettings()),
                conceptSubjectRepository);

        conceptSubjectService.harvestAndSave();
    }

    @Test
    void test_if_concept_subjects_query_returns_all_concept_subjects() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/concept-subjects.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://catalog-admin-service.staging.fellesdatakatalog.digdir.no/123456789/concepts/subjects#1", response.get("$['data']['conceptSubjects'][0]['uri']"));
        assertEquals("1", response.get("$['data']['conceptSubjects'][0]['code']"));
        assertEquals("en 1", response.get("$['data']['conceptSubjects'][0]['label']['en']"));
    }

}
