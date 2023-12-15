package no.fdk.referencedata.geonorge.administrativeenheter.kommune;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.eu.accessright.AccessRight;
import no.fdk.referencedata.eu.accessright.AccessRightRepository;
import no.fdk.referencedata.eu.accessright.AccessRightService;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.settings.Settings.ACCESS_RIGHT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class KommuneServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private KommuneRepository kommuneRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @Test
    public void test_if_harvest_persists_kommuner() {
        KommuneService kommuneService = new KommuneService(
                new LocalKommuneHarvester(wiremockHost, wiremockPort),
                kommuneRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        kommuneService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        kommuneRepository.findAll().forEach(accessRight -> counter.incrementAndGet());
        assertEquals(4, counter.get());

        final Kommune first = kommuneRepository.findById("https://data.geonorge.no/administrativeEnheter/kommune/id/123456").orElseThrow();
        assertEquals("https://data.geonorge.no/administrativeEnheter/kommune/id/123456", first.getUri());
        assertEquals("Kommune 1", first.getKommunenavn());
        assertEquals("Kommune 1 norsk", first.getKommunenavnNorsk());
        assertEquals("123456", first.getKommunenummer());
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        KommuneRepository kommuneRepositorySpy = spy(this.kommuneRepository);

        Kommune kommune = Kommune.builder()
                .uri("http://uri.no")
                .kommunenavn("KOMMUNENAVN")
                .kommunenavnNorsk("KOMMUNENAVNNORSK")
                .kommunenummer("KOMMUNENR")
                .build();
        kommuneRepositorySpy.save(kommune);


        long count = kommuneRepositorySpy.count();
        assertTrue(count > 0);

        when(kommuneRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        KommuneService kommuneService = new KommuneService(
                new LocalKommuneHarvester(wiremockHost, wiremockPort),
                kommuneRepositorySpy,
                rdfSourceRepository,
                harvestSettingsRepository);

        assertEquals(count, kommuneRepositorySpy.count());
    }
}
