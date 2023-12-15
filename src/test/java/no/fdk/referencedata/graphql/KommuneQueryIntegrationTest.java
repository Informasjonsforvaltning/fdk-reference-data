package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.jayway.jsonpath.PathNotFoundException;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneRepository;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneService;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.LocalKommuneHarvester;
import no.fdk.referencedata.iana.mediatype.LocalMediaTypeHarvester;
import no.fdk.referencedata.iana.mediatype.MediaTypeService;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
class KommuneQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @Autowired
    private KommuneRepository kommuneRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private GraphQLTestTemplate template;

    @BeforeEach
    public void setup() {
        KommuneService kommuneService = new KommuneService(
                new LocalKommuneHarvester(wiremockHost, wiremockPort),
                kommuneRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        kommuneService.harvestAndSave();
    }

    @Test
    void test_if_kommune_query_returns_all_kommuner() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/kommuner.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.geonorge.no/administrativeEnheter/kommune/id/323456", response.get("$['data']['kommuner'][2]['uri']"));
        assertEquals("Kommune 3", response.get("$['data']['kommuner'][2]['kommunenavn']"));
        assertEquals("Kommune 3 norsk", response.get("$['data']['kommuner'][2]['kommunenavnNorsk']"));
        assertEquals("323456", response.get("$['data']['kommuner'][2]['kommunenummer']"));
        assertThrows(PathNotFoundException.class, () -> response.get("$['data']['kommuner'][4]"));
    }

    @Test
    void test_if_kommune_by_kommunesnummer_query_returns_norge() throws IOException {
        GraphQLResponse response = template.perform("graphql/kommune-by-kommunenummer.graphql",
                mapper.valueToTree(Map.of("kommunenummer", "223456")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.geonorge.no/administrativeEnheter/kommune/id/223456", response.get("$['data']['kommuneByKommunenummer']['uri']"));
        assertThrows(PathNotFoundException.class, () -> response.get("$['data']['kommuneByKommunenummer']['kommunenavn']"));
    }

    @Test
    void test_if_kommune_by_kommunesnummer_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/kommune-by-kommunenummer.graphql",
                mapper.valueToTree(Map.of("kommunenummer", "11111")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['kommuneByKommunenummer']"));
    }
}
