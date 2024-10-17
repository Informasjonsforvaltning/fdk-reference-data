package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.Kommune;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneRepository;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneService;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.LocalKommuneHarvester;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class KommuneQueryIntegrationTest extends AbstractContainerTest {

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
    private GraphQlTester graphQlTester;

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
    void test_if_kommune_query_returns_all_kommuner() {
        List<Kommune> result = graphQlTester.documentName("kommuner")
                .execute()
                .path("$['data']['kommuner']")
                .entityList(Kommune.class)
                .get();

        Assertions.assertEquals(4, result.size());

        Kommune kommune = result.get(2);

        assertEquals("https://data.geonorge.no/administrativeEnheter/kommune/id/323456", kommune.getUri());
        assertEquals("Kommune 3", kommune.getKommunenavn());
        assertEquals("Kommune 3 norsk", kommune.getKommunenavnNorsk());
        assertEquals("323456", kommune.getKommunenummer());
    }

    @Test
    void test_if_kommune_by_kommunesnummer_query_returns_norge() {
            Kommune result = graphQlTester.documentName("kommune-by-kommunenummer")
                .variable("kommunenummer", "223456")
                .execute()
                .path("$['data']['kommuneByKommunenummer']")
                .entity(Kommune.class)
                .get();

        assertEquals("https://data.geonorge.no/administrativeEnheter/kommune/id/223456", result.getUri());
    }

    @Test
    void test_if_kommune_by_kommunesnummer_query_returns_null() {
        graphQlTester.documentName("kommune-by-kommunenummer")
                .variable("kommunenummer", "11111")
                .execute()
                .path("$['data']['kommuneByKommunenummer']")
                .valueIsNull();
    }
}
