package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.ssb.fylkeorganisasjoner.FylkeOrganisasjon;
import no.fdk.referencedata.ssb.fylkeorganisasjoner.FylkeOrganisasjonRepository;
import no.fdk.referencedata.ssb.fylkeorganisasjoner.FylkeOrganisasjonService;
import no.fdk.referencedata.ssb.fylkeorganisasjoner.LocalFylkeOrganisasjonHarvester;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class FylkeOrganisasjonQueryIntegrationTest extends AbstractContainerTest {

    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @Autowired
    private FylkeOrganisasjonRepository fylkeOrganisasjonRepository;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        FylkeOrganisasjonService fylkeOrganisasjonService = new FylkeOrganisasjonService(
                new LocalFylkeOrganisasjonHarvester(wiremockHost, wiremockPort),
                fylkeOrganisasjonRepository);

        fylkeOrganisasjonService.harvestAndSave();
    }

    @Test
    void test_if_fylke_organisasjoner_query_returns_all_fylkeorganisasjoner() {
        List<FylkeOrganisasjon> result = graphQlTester.documentName("fylkeorganisasjoner")
                .execute()
                .path("$['data']['fylkeOrganisasjoner']")
                .entityList(FylkeOrganisasjon.class)
                .get();

        Assertions.assertEquals(2, result.size());

        FylkeOrganisasjon fylkeOrganisasjon = result.get(0);

        assertEquals("https://data.brreg.no/enhetsregisteret/api/enheter/817920632", fylkeOrganisasjon.getUri());
        assertEquals("Fylke 1", fylkeOrganisasjon.getFylkesnavn());
        assertEquals("50", fylkeOrganisasjon.getFylkesnummer());
        assertEquals("FYLKE 1 FYLKESKOMMUNE", fylkeOrganisasjon.getOrganisasjonsnavn());
        assertEquals("817920632", fylkeOrganisasjon.getOrganisasjonsnummer());
        assertEquals("https://data.brreg.no/enhetsregisteret/api/enheter/821227062", result.get(1).getUri());
    }

    @Test
    void test_if_fylkeorganisasjon_by_fylkesnummer_query_returns_correct_value() {
        FylkeOrganisasjon result = graphQlTester.documentName("fylkeorganisasjon-by-fylkesnummer")
                .variable("fylkesnummer", "38")
                .execute()
                .path("$['data']['fylkeOrganisasjonByFylkesnummer']")
                .entity(FylkeOrganisasjon.class)
                .get();

        assertEquals("https://data.brreg.no/enhetsregisteret/api/enheter/821227062", result.getUri());
        assertEquals("38", result.getFylkesnummer());
        assertEquals("821227062", result.getOrganisasjonsnummer());
        assertNull(result.getFylkesnavn());
    }

    @Test
    void test_if_fylkeorganisasjon_by_fylkesnummer_query_returns_null() {
        graphQlTester.documentName("fylkeorganisasjon-by-fylkesnummer")
                .variable("fylkesnummer", "11111")
                .execute()
                .path("$['data']['fylkeOrganisasjonByFylkesnummer']")
                .valueIsNull();
    }
}
