package no.fdk.referencedata.geonorge.administrativeenheter.fylke;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.Fylke;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.FylkeRepository;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.FylkeService;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class FylkeServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private FylkeRepository fylkeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @Test
    public void test_if_harvest_persists_fylker() {
        FylkeService fylkeService = new FylkeService(
                new LocalFylkeHarvester(wiremockHost, wiremockPort),
                fylkeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        fylkeService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        fylkeRepository.findAll().forEach(accessRight -> counter.incrementAndGet());
        assertEquals(4, counter.get());

        final Fylke first = fylkeRepository.findById("https://data.geonorge.no/administrativeEnheter/fylke/id/123456").orElseThrow();
        assertEquals("https://data.geonorge.no/administrativeEnheter/fylke/id/123456", first.getUri());
        assertEquals("Fylke 1", first.getFylkesnavn());
        assertEquals("123456", first.getFylkesnummer());
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        FylkeRepository fylkeRepositorySpy = spy(this.fylkeRepository);

        Fylke fylke = Fylke.builder()
                .uri("http://uri.no")
                .fylkesnavn("FYLKESNAVN")
                .fylkesnummer("KOMMUNENR")
                .build();
        fylkeRepositorySpy.save(fylke);


        long count = fylkeRepositorySpy.count();
        assertTrue(count > 0);

        when(fylkeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        FylkeService fylkeService = new FylkeService(
                new LocalFylkeHarvester(wiremockHost, wiremockPort),
                fylkeRepositorySpy,
                rdfSourceRepository,
                harvestSettingsRepository);

        assertEquals(count, fylkeRepositorySpy.count());
    }
}
