package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.eu.frequency.FrequencyRepository;
import no.fdk.referencedata.eu.frequency.FrequencyService;
import no.fdk.referencedata.eu.frequency.LocalFrequencyHarvester;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class FrequencyQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private FrequencyRepository frequencyRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private GraphQLTestTemplate template;

    @BeforeEach
    public void setup() {
        FrequencyService frequencyService = new FrequencyService(
                new LocalFrequencyHarvester("1"),
                frequencyRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        frequencyService.harvestAndSave(false);
    }

    @Test
    void test_if_frequencies_query_returns_all_frequencies() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/frequencies.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://publications.europa.eu/resource/authority/frequency/ANNUAL", response.get("$['data']['frequencies'][0]['uri']"));
        assertEquals("ANNUAL", response.get("$['data']['frequencies'][0]['code']"));
        assertEquals("annual", response.get("$['data']['frequencies'][0]['label']['en']"));
    }

    @Test
    void test_if_frequency_by_code_public_query_returns_public_frequency() throws IOException {
        GraphQLResponse response = template.perform("graphql/frequency-by-code.graphql",
                mapper.valueToTree(Map.of("code", "WEEKLY_3")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://publications.europa.eu/resource/authority/frequency/WEEKLY_3", response.get("$['data']['frequencyByCode']['uri']"));
        assertEquals("WEEKLY_3", response.get("$['data']['frequencyByCode']['code']"));
        assertEquals("three times a week", response.get("$['data']['frequencyByCode']['label']['en']"));
    }

    @Test
    void test_if_frequency_by_code_unknown_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/frequency-by-code.graphql",
                mapper.valueToTree(Map.of("code", "unknown")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['frequencyByCode']"));
    }

}
