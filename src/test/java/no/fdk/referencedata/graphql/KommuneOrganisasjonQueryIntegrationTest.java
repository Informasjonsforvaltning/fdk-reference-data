package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.jayway.jsonpath.PathNotFoundException;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.ssb.kommuneorganisasjoner.KommuneOrganisasjonRepository;
import no.fdk.referencedata.ssb.kommuneorganisasjoner.KommuneOrganisasjonService;
import no.fdk.referencedata.ssb.kommuneorganisasjoner.LocalKommuneOrganisasjonHarvester;
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
class KommuneOrganisasjonQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @Autowired
    private KommuneOrganisasjonRepository kommuneOrganisasjonRepository;

    @Autowired
    private GraphQLTestTemplate template;

    @BeforeEach
    public void setup() {
        KommuneOrganisasjonService kommuneOrganisasjonService = new KommuneOrganisasjonService(
                new LocalKommuneOrganisasjonHarvester(wiremockHost, wiremockPort),
                kommuneOrganisasjonRepository);

        kommuneOrganisasjonService.harvestAndSave();
    }

    @Test
    void test_if_kommune_organisasjoner_query_returns_all_kommuneorganisasjoner() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/kommuneorganisasjoner.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.brreg.no/enhetsregisteret/api/enheter/817263992", response.get("$['data']['kommuneOrganisasjoner'][0]['uri']"));
        assertEquals("Kommune 1", response.get("$['data']['kommuneOrganisasjoner'][0]['kommunenavn']"));
        assertEquals("3811", response.get("$['data']['kommuneOrganisasjoner'][0]['kommunenummer']"));
        assertEquals("KOMMUNE 1 KOMMUNE", response.get("$['data']['kommuneOrganisasjoner'][0]['organisasjonsnavn']"));
        assertEquals("817263992", response.get("$['data']['kommuneOrganisasjoner'][0]['organisasjonsnummer']"));
        assertEquals("https://data.brreg.no/enhetsregisteret/api/enheter/997391853", response.get("$['data']['kommuneOrganisasjoner'][1]['uri']"));
        assertThrows(PathNotFoundException.class, () -> response.get("$['data']['kommuneOrganisasjoner'][2]"));
    }

    @Test
    void test_if_kommuneorganisasjon_by_kommunesnummer_query_returns_correct_value() throws IOException {
        GraphQLResponse response = template.perform("graphql/kommuneorganisasjon-by-kommunenummer.graphql",
                mapper.valueToTree(Map.of("kommunenummer", "5053")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.brreg.no/enhetsregisteret/api/enheter/997391853", response.get("$['data']['kommuneOrganisasjonByKommunenummer']['uri']"));
        assertEquals("5053", response.get("$['data']['kommuneOrganisasjonByKommunenummer']['kommunenummer']"));
        assertEquals("997391853", response.get("$['data']['kommuneOrganisasjonByKommunenummer']['organisasjonsnummer']"));
        assertThrows(PathNotFoundException.class, () -> response.get("$['data']['kommuneOrganisasjonByKommunenummer']['kommunenavn']"));
    }

    @Test
    void test_if_kommuneorganisasjon_by_kommunesnummer_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/kommuneorganisasjon-by-kommunenummer.graphql",
                mapper.valueToTree(Map.of("kommunenummer", "11111")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['kommuneOrganisasjonByKommunenummer']"));
    }
}
