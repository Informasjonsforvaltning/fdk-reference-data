package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.Fylke;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.FylkeRepository;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.FylkeService;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.LocalFylkeHarvester;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class FylkeQueryIntegrationTest extends AbstractContainerTest {

    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @Autowired
    private FylkeRepository fylkeRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        FylkeService fylkeService = new FylkeService(
                new LocalFylkeHarvester(wiremockHost, wiremockPort),
                fylkeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        fylkeService.harvestAndSave();
    }

    @Test
    void test_if_fylke_query_returns_all_fylker() {
        List<Fylke> result = graphQlTester.documentName("fylker")
                .execute()
                .path("$['data']['fylker']")
                .entityList(Fylke.class)
                .get();

        Assertions.assertEquals(4, result.size());

        Fylke fylke = result.get(0);

        assertEquals("https://data.geonorge.no/administrativeEnheter/fylke/id/123456", fylke.getUri());
        assertEquals("Fylke 1", fylke.getFylkesnavn());
        assertEquals("123456", fylke.getFylkesnummer());
    }

    @Test
    void test_if_fylke_by_fylkesnummer_query_returns_norge() {
        Fylke result = graphQlTester.documentName("fylke-by-fylkesnummer")
                .variable("fylkesnummer", "223456")
                .execute()
                .path("$['data']['fylkeByFylkesnummer']")
                .entity(Fylke.class)
                .get();

        assertEquals("https://data.geonorge.no/administrativeEnheter/fylke/id/223456", result.getUri());
        assertNull(result.getFylkesnavn());
    }

    @Test
    void test_if_fylke_by_fylkesnummer_query_returns_null() {
        graphQlTester.documentName("fylke-by-fylkesnummer")
                .variable("fylkesnummer", "11111")
                .execute()
                .path("$['data']['fylkeByFylkesnummer']")
                .valueIsNull();
    }
}
