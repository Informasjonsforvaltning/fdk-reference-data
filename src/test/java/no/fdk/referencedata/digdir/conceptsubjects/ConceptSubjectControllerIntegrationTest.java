package no.fdk.referencedata.digdir.conceptsubjects;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.ApplicationSettings;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class ConceptSubjectControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ConceptSubjectRepository conceptSubjectRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        ConceptSubjectService conceptSubjectService = new ConceptSubjectService(
                LocalHarvesters.conceptSubject(new ApplicationSettings()),
                conceptSubjectRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        conceptSubjectService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_concept_subjects_returns_valid_response() {
        ConceptSubjects conceptSubjects =
                restClient.get().uri("/digdir/concept-subjects").retrieve().body(ConceptSubjects.class);
        ConceptSubject first = conceptSubjects.getConceptSubjects().get(0);
        assertEquals("https://catalog-admin-service.staging.fellesdatakatalog.digdir.no/123456789/concepts/subjects#1", first.getUri());
        assertEquals("1", first.getCode());
        assertEquals("en 1", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_concept_subjects_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/digdir/concept-subjects", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(ConceptSubjectControllerIntegrationTest.class.getClassLoader().getResource("concept-subjects.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
