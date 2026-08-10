package no.fdk.referencedata.eu.language;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
                "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class LanguageControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        LanguageService languageService = new LanguageService(
                LocalHarvesters.language(),
                languageRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        languageService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_languages_returns_valid_response() {
        Languages languages =
                restClient.get().uri("/eu/languages").retrieve().body(Languages.class);
        Language first = languages.getLanguages().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/language/ENG", first.getUri());
        assertEquals("ENG", first.getCode());
        assertEquals("English", first.getLabel().get(no.fdk.referencedata.i18n.Language.ENGLISH.code()));
        assertEquals("engelsk", first.getLabel().get(no.fdk.referencedata.i18n.Language.NORWEGIAN.code()));
    }

    @Test
    public void test_if_get_language_by_code_returns_valid_response() {
        Language language =
                restClient.get().uri("/eu/languages/NOB").retrieve().body(Language.class);

        assertNotNull(language);
        assertEquals("http://publications.europa.eu/resource/authority/language/NOB", language.getUri());
        assertEquals("NOB", language.getCode());
        assertEquals("Norwegian Bokmål", language.getLabel().get(no.fdk.referencedata.i18n.Language.ENGLISH.code()));
        assertEquals("norsk (bokmål)", language.getLabel().get(no.fdk.referencedata.i18n.Language.NORWEGIAN.code()));
    }

    @Test
    public void test_languages_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/languages", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(LanguageControllerIntegrationTest.class.getClassLoader().getResource("languages-translated.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
