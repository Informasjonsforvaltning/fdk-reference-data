package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.jayway.jsonpath.PathNotFoundException;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.ssb.fylkeorganisasjoner.FylkeOrganisasjonRepository;
import no.fdk.referencedata.ssb.fylkeorganisasjoner.FylkeOrganisasjonService;
import no.fdk.referencedata.ssb.fylkeorganisasjoner.LocalFylkeOrganisasjonHarvester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class FylkeOrganisasjonQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @Autowired
    private FylkeOrganisasjonRepository fylkeOrganisasjonRepository;

    @Autowired
    private GraphQLTestTemplate template;

    @BeforeEach
    public void setup() {
        FylkeOrganisasjonService fylkeOrganisasjonService = new FylkeOrganisasjonService(
                new LocalFylkeOrganisasjonHarvester(wiremockHost, wiremockPort),
                fylkeOrganisasjonRepository);

        fylkeOrganisasjonService.harvestAndSave();
    }

    @Test
    void test_if_fylke_organisasjoner_query_returns_all_fylkeorganisasjoner() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/fylkeorganisasjoner.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.brreg.no/enhetsregisteret/api/enheter/817920632", response.get("$['data']['fylkeOrganisasjoner'][0]['uri']"));
        assertEquals("Fylke 1", response.get("$['data']['fylkeOrganisasjoner'][0]['fylkesnavn']"));
        assertEquals("50", response.get("$['data']['fylkeOrganisasjoner'][0]['fylkesnummer']"));
        assertEquals("FYLKE 1 FYLKESKOMMUNE", response.get("$['data']['fylkeOrganisasjoner'][0]['organisasjonsnavn']"));
        assertEquals("817920632", response.get("$['data']['fylkeOrganisasjoner'][0]['organisasjonsnummer']"));
        assertEquals("https://data.brreg.no/enhetsregisteret/api/enheter/821227062", response.get("$['data']['fylkeOrganisasjoner'][1]['uri']"));
        assertThrows(PathNotFoundException.class, () -> response.get("$['data']['fylkeOrganisasjoner'][2]"));
    }

    @Test
    void test_if_fylkeorganisasjon_by_fylkesnummer_query_returns_correct_value() throws IOException {
        GraphQLResponse response = template.perform("graphql/fylkeorganisasjon-by-fylkesnummer.graphql",
                mapper.valueToTree(Map.of("fylkesnummer", "38")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.brreg.no/enhetsregisteret/api/enheter/821227062", response.get("$['data']['fylkeOrganisasjonByFylkesnummer']['uri']"));
        assertEquals("38", response.get("$['data']['fylkeOrganisasjonByFylkesnummer']['fylkesnummer']"));
        assertEquals("821227062", response.get("$['data']['fylkeOrganisasjonByFylkesnummer']['organisasjonsnummer']"));
        assertThrows(PathNotFoundException.class, () -> response.get("$['data']['fylkeOrganisasjonByFylkesnummer']['fylkesnavn']"));
    }

    @Test
    void test_if_fylkeorganisasjon_by_fylkesnummer_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/fylkeorganisasjon-by-fylkesnummer.graphql",
                mapper.valueToTree(Map.of("fylkesnummer", "11111")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['fylkeOrganisasjonByFylkesnummer']"));
    }
}
