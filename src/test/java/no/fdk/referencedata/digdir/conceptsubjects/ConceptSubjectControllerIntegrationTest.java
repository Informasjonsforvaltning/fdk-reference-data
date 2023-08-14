package no.fdk.referencedata.digdir.conceptsubjects;

import no.fdk.referencedata.ApplicationSettings;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
            "application.catalogAdminUri=http://localhost:8080",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class ConceptSubjectControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ConceptSubjectRepository conceptSubjectRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        ConceptSubjectService conceptSubjectService = new ConceptSubjectService(
                new LocalConceptSubjectHarvester(new ApplicationSettings()),
                conceptSubjectRepository);

        conceptSubjectService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_concept_subjects_returns_valid_response() {
        ConceptSubjects conceptSubjects =
                this.restTemplate.getForObject("http://localhost:" + port + "/digdir/concept-subjects", ConceptSubjects.class);

        assertEquals(4, conceptSubjects.getConceptSubjects().size());

        ConceptSubject first = conceptSubjects.getConceptSubjects().get(0);
        assertEquals("https://catalog-admin-service.staging.fellesdatakatalog.digdir.no/123456789/concepts/subjects#1", first.getUri());
        assertEquals("1", first.getCode());
        assertEquals("en 1", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_post_concept_subjects_fails_without_api_key() {
        assertEquals(4, conceptSubjectRepository.count());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/digdir/concept-subjects",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(4, conceptSubjectRepository.count());
    }

    @Test
    public void test_if_post_concept_subjects_runs_ok() {
        assertEquals(4, conceptSubjectRepository.count());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/digdir/concept-subjects",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, conceptSubjectRepository.count());
    }
}
