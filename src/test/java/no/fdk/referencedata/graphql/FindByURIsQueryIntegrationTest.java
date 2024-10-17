package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.FylkeRepository;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.FylkeService;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.LocalFylkeHarvester;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneRepository;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneService;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.LocalKommuneHarvester;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.search.FindByURIsRequest;
import no.fdk.referencedata.search.SearchHit;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Stream;

import static no.fdk.referencedata.search.SearchAlternative.ADMINISTRATIVE_ENHETER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class FindByURIsQueryIntegrationTest extends AbstractContainerTest {

    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @Autowired
    private FylkeRepository fylkeRepository;

    @Autowired
    private KommuneRepository kommuneRepository;

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

        KommuneService kommuneService = new KommuneService(
                new LocalKommuneHarvester(wiremockHost, wiremockPort),
                kommuneRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        kommuneService.harvestAndSave();
    }

    @Test
    void test_if_find_by_uris_query_returns_combined_location_hits() {
        List<String> expectedURIs = List.of(
                "https://data.geonorge.no/administrativeEnheter/fylke/id/123456",
                "https://data.geonorge.no/administrativeEnheter/kommune/id/123456",
                "https://data.geonorge.no/administrativeEnheter/kommune/id/323456",
                "https://data.geonorge.no/administrativeEnheter/nasjon/id/173163"
        );
        FindByURIsRequest req = FindByURIsRequest.builder().uris(expectedURIs).types(List.of(ADMINISTRATIVE_ENHETER)).build();

        List<SearchHit> actual = graphQlTester.documentName("find-by-uris")
                .variable("req", req)
                .execute()
                .path("$['data']['findByURIs']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(4, actual.size());

        List<String> actualURIs = Stream.of(
                actual.get(0).getUri(),
                actual.get(1).getUri(),
                actual.get(2).getUri(),
                actual.get(3).getUri()
        ).sorted().toList();

        assertEquals(expectedURIs, actualURIs);
    }
}
