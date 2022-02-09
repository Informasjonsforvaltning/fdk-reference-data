package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.jayway.jsonpath.PathNotFoundException;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.iana.mediatype.LocalMediaTypeHarvester;
import no.fdk.referencedata.iana.mediatype.MediaTypeRepository;
import no.fdk.referencedata.iana.mediatype.MediaTypeService;
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
class NasjonQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private GraphQLTestTemplate template;

    @Test
    void test_if_nasjon_query_returns_all_nasjoner() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/nasjoner.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.geonorge.no/administrativeEnheter/nasjon/id/173163", response.get("$['data']['nasjoner'][0]['uri']"));
        assertEquals("Norge", response.get("$['data']['nasjoner'][0]['nasjonsnavn']"));
        assertEquals("173163", response.get("$['data']['nasjoner'][0]['nasjonsnummer']"));
        assertThrows(PathNotFoundException.class, () -> response.get("$['data']['nasjoner'][1]"));
    }

    @Test
    void test_if_nasjon_by_nasjonsnummer_query_returns_norge() throws IOException {
        GraphQLResponse response = template.perform("graphql/nasjon-by-nasjonsnummer.graphql",
                mapper.valueToTree(Map.of("nasjonsnummer", "173163")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.geonorge.no/administrativeEnheter/nasjon/id/173163", response.get("$['data']['nasjonByNasjonsnummer']['uri']"));
        assertThrows(PathNotFoundException.class, () -> response.get("$['data']['nasjonByNasjonsnummer']['nasjonsnavn']"));
    }

    @Test
    void test_if_nasjon_by_nasjonsnummer_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/nasjon-by-nasjonsnummer.graphql",
                mapper.valueToTree(Map.of("nasjonsnummer", "11111")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['nasjonByNasjonsnummer']"));
    }
}
