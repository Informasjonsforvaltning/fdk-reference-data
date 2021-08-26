package no.fdk.referencedata.graphql;

import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import no.fdk.referencedata.eu.eurovoc.EuroVocRepository;
import no.fdk.referencedata.eu.eurovoc.EuroVocService;
import no.fdk.referencedata.eu.eurovoc.LocalEuroVocHarvester;
import no.fdk.referencedata.mongo.AbstractMongoDbContainerTest;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
class EuroVocQueryIntegrationTest extends AbstractMongoDbContainerTest {

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

        EuroVocService.harvestAndSave();
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
        GraphQLResponse response = template.postForResource("graphql/eurovoc-by-code.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://eurovoc.europa.eu/5548", response.get("$['data']['euroVocByCode']['uri']"));
        assertEquals("5548", response.get("$['data']['euroVocByCode']['code']"));
        assertEquals("interinstitutional cooperation (EU)", response.get("$['data']['euroVocByCode']['label']['en']"));
    }

    @Test
    void test_if_eurovoc_by_code_unknown_query_returns_null() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/eurovoc-by-code-unknown.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['euroVocByCode']"));
    }

}
