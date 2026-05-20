package no.fdk.referencedata.geonames;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class GeonamesServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GeonamesFylkeRepository geonamesFylkeRepository;

    @Autowired
    private GeonamesKommuneRepository geonamesKommuneRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @Test
    public void test_if_harvest_persists_fylker_and_kommuner() {
        GeonamesService geonamesService = new GeonamesService(
                new LocalGeonamesHarvester(wiremockHost, wiremockPort),
                geonamesFylkeRepository,
                geonamesKommuneRepository,
                rdfSourceRepository,
                new GeonamesWriter(geonamesFylkeRepository, geonamesKommuneRepository, rdfSourceRepository));

        geonamesService.harvestAndSave();

        assertEquals(2, geonamesFylkeRepository.count());
        assertEquals(4, geonamesKommuneRepository.count());

        GeonamesFylke agder = geonamesFylkeRepository.findByGeonameId("7626836").orElseThrow();
        assertEquals("https://sws.geonames.org/7626836/", agder.getUri());
        assertEquals("Agder", agder.getName());

        GeonamesKommune kristiansand = geonamesKommuneRepository.findByGeonameId("7626837").orElseThrow();
        assertEquals("https://sws.geonames.org/7626837/", kristiansand.getUri());
        assertEquals("Kristiansand", kristiansand.getName());
        assertEquals("7626836", kristiansand.getFylkeGeonameId());
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        GeonamesFylkeRepository geonamesFylkeRepositorySpy = spy(this.geonamesFylkeRepository);

        GeonamesFylke fylke = GeonamesFylke.builder()
                .uri("https://sws.geonames.org/9999/")
                .geonameId("9999")
                .name("Test Fylke")
                .build();
        geonamesFylkeRepositorySpy.save(fylke);

        long count = geonamesFylkeRepositorySpy.count();
        assertTrue(count > 0);

        when(geonamesFylkeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new GeonamesService(
                new LocalGeonamesHarvester(wiremockHost, wiremockPort),
                geonamesFylkeRepositorySpy,
                geonamesKommuneRepository,
                rdfSourceRepository,
                new GeonamesWriter(geonamesFylkeRepositorySpy, geonamesKommuneRepository, rdfSourceRepository));

        assertEquals(count, geonamesFylkeRepositorySpy.count());
    }

    @Test
    public void test_rdf_is_stored_after_harvest() {
        GeonamesService geonamesService = new GeonamesService(
                new LocalGeonamesHarvester(wiremockHost, wiremockPort),
                geonamesFylkeRepository,
                geonamesKommuneRepository,
                rdfSourceRepository,
                new GeonamesWriter(geonamesFylkeRepository, geonamesKommuneRepository, rdfSourceRepository));

        geonamesService.harvestAndSave();

        String rdf = geonamesService.getRdf(org.apache.jena.riot.RDFFormat.TURTLE);
        assertNotNull(rdf);
        assertTrue(rdf.contains("sws.geonames.org"));
    }
}
