package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.eu.eurovoc.EuroVocRepository;
import no.fdk.referencedata.eu.eurovoc.EuroVocService;
import no.fdk.referencedata.eu.eurovoc.LocalEuroVocHarvester;
import no.fdk.referencedata.container.AbstractContainerTest;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class EuroVocQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private GraphQLTestTemplate template;

    @Autowired
    private EuroVocRepository euroVocRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @BeforeEach
    public void setup() {
        EuroVocService EuroVocService = new EuroVocService(
                new LocalEuroVocHarvester("1"),
                euroVocRepository,
                harvestSettingsRepository);

        EuroVocService.harvestAndSave(false);
    }

    @Test
    void test_if_eurovocs_query_returns_all_eurovocs() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/eurovocs.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://eurovoc.europa.eu/1", response.get("$['data']['euroVocs'][0]['uri']"));
        assertEquals("1", response.get("$['data']['euroVocs'][0]['code']"));
        assertEquals("Århus (county)", response.get("$['data']['euroVocs'][0]['label']['en']"));
    }

    @Test
    void test_if_eurovoc_by_code_5548_query_returns_interinstitutional_cooperation_eu() throws IOException {
        GraphQLResponse response = template.perform("graphql/eurovoc-by-code.graphql",
                mapper.valueToTree(Map.of("code", "337")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://eurovoc.europa.eu/337", response.get("$['data']['euroVocByCode']['uri']"));
        assertEquals("337", response.get("$['data']['euroVocByCode']['code']"));
        assertEquals("regions of Denmark", response.get("$['data']['euroVocByCode']['label']['en']"));
    }

    @Test
    void test_if_eurovoc_by_code_unknown_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/eurovoc-by-code.graphql",
                mapper.valueToTree(Map.of("code", "unknown")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['euroVocByCode']"));
    }

}
