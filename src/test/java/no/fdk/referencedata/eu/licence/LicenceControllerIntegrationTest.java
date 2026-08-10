package no.fdk.referencedata.eu.licence;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.i18n.Language;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class LicenceControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private LicenceRepository licenceRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        LicenceService licenceService = new LicenceService(
                LocalHarvesters.licence(),
                licenceRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        licenceService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_licences_returns_valid_response() {
        Licences licences =
                restClient.get()
                        .uri("/eu/licences")
                        .retrieve()
                        .body(Licences.class);
        Licence first = licences.getLicences().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/licence/0BSD", first.getUri());
        assertEquals("0BSD", first.getCode());
        assertEquals("Zero-Clause BSD / Free Public License 1.0.0 (0BSD)", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_licence_by_code_returns_valid_response() {
        Licence licence =
                restClient.get()
                        .uri("/eu/licences/CC0")
                        .retrieve()
                        .body(Licence.class);

        assertNotNull(licence);
        assertEquals("http://publications.europa.eu/resource/authority/licence/CC0", licence.getUri());
        assertEquals("CC0", licence.getCode());
        assertEquals("Creative Commons CC0 1.0 Universal", licence.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_licences_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/licences", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(LicenceControllerIntegrationTest.class.getClassLoader().getResource("licences-sparql-result.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
} 
