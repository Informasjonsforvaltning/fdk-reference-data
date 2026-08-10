package no.fdk.referencedata.eu.frequency;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.core.ReferenceDataWriter;
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
public class FrequencyControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private FrequencyRepository frequencyRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        FrequencyService frequencyService = new FrequencyService(
                LocalHarvesters.frequency(),
                frequencyRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        frequencyService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_frequencies_returns_valid_response() {
        Frequencies frequencies =
                restClient.get()
                        .uri("/eu/frequencies")
                        .retrieve()
                        .body(Frequencies.class);
        Frequency first = frequencies.getFrequencies().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/frequency/CONT", first.getUri());
        assertEquals("CONT", first.getCode());
        assertEquals("kontinuerleg", first.getLabel().get(Language.NORWEGIAN_NYNORSK.code()));
    }

    @Test
    public void test_if_get_frequency_by_code_returns_valid_response() {
        Frequency frequency =
                restClient.get()
                        .uri("/eu/frequencies/ANNUAL")
                        .retrieve()
                        .body(Frequency.class);

        assertNotNull(frequency);
        assertEquals("http://publications.europa.eu/resource/authority/frequency/ANNUAL", frequency.getUri());
        assertEquals("ANNUAL", frequency.getCode());
        assertEquals("annual", frequency.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_frequencies_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/frequencies", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(FrequencyControllerIntegrationTest.class.getClassLoader().getResource("frequencies-sparql-result.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
