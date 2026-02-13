package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.ssb.kommuneorganisasjoner.KommuneOrganisasjon;
import no.fdk.referencedata.ssb.kommuneorganisasjoner.KommuneOrganisasjonRepository;
import no.fdk.referencedata.ssb.kommuneorganisasjoner.KommuneOrganisasjonService;
import no.fdk.referencedata.ssb.kommuneorganisasjoner.LocalKommuneOrganisasjonHarvester;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class KommuneOrganisasjonQueryIntegrationTest extends AbstractContainerTest {

    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @Autowired
    private KommuneOrganisasjonRepository kommuneOrganisasjonRepository;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        KommuneOrganisasjonService kommuneOrganisasjonService = new KommuneOrganisasjonService(
                new LocalKommuneOrganisasjonHarvester(wiremockHost, wiremockPort),
                kommuneOrganisasjonRepository);

        kommuneOrganisasjonService.harvestAndSave();
    }

    @Test
    void test_if_kommune_organisasjoner_query_returns_all_kommuneorganisasjoner() {
        List<KommuneOrganisasjon> result = graphQlTester.documentName("kommuneorganisasjoner")
                .execute()
                .path("$['data']['kommuneOrganisasjoner']")
                .entityList(KommuneOrganisasjon.class)
                .get();

        Assertions.assertEquals(2, result.size());

        assertEquals("https://data.brreg.no/enhetsregisteret/api/enheter/817263992", result.get(0).getUri());
        assertEquals("Kommune 1", result.get(0).getKommunenavn());
        assertEquals("3811", result.get(0).getKommunenummer());
        assertEquals("KOMMUNE 1 KOMMUNE", result.get(0).getOrganisasjonsnavn());
        assertEquals("817263992", result.get(0).getOrganisasjonsnummer());
        assertEquals("https://data.brreg.no/enhetsregisteret/api/enheter/997391853", result.get(1).getUri());
    }

    @Test
    void test_if_kommuneorganisasjon_by_kommunesnummer_query_returns_correct_value() {
        KommuneOrganisasjon result = graphQlTester.documentName("kommuneorganisasjon-by-kommunenummer")
                .variable("kommunenummer", "5053")
                .execute()
                .path("$['data']['kommuneOrganisasjonByKommunenummer']")
                .entity(KommuneOrganisasjon.class)
                .get();

        assertEquals("https://data.brreg.no/enhetsregisteret/api/enheter/997391853", result.getUri());
        assertEquals("5053", result.getKommunenummer());
        assertEquals("997391853", result.getOrganisasjonsnummer());
    }

    @Test
    void test_if_kommuneorganisasjon_by_kommunesnummer_query_returns_null() {
        graphQlTester.documentName("kommuneorganisasjon-by-kommunenummer")
                .variable("kommunenummer", "11111")
                .execute()
                .path("$['data']['kommuneOrganisasjonByKommunenummer']")
                .valueIsNull();
    }
}
