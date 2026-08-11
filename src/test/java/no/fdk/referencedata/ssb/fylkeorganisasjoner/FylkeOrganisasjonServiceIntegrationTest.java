package no.fdk.referencedata.ssb.fylkeorganisasjoner;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import no.fdk.referencedata.core.HarvestMetrics;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.rdf.RDFSourceRepository;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.container.AbstractContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class FylkeOrganisasjonServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private FylkeOrganisasjonRepository fylkeOrganisasjonRepository;

    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @Test
    public void test_if_harvest_persists_fylker() {
        FylkeOrganisasjonService fylkeOrganisasjonService = new FylkeOrganisasjonService(
                new LocalFylkeOrganisasjonHarvester(wiremockHost, wiremockPort),
                fylkeOrganisasjonRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(mock(RDFSourceRepository.class)), mock(RDFSourceRepository.class), new HarvestMetrics(new SimpleMeterRegistry())));

        fylkeOrganisasjonService.harvestAndSave();

        final FylkeOrganisasjon first = fylkeOrganisasjonRepository.findById("https://data.brreg.no/enhetsregisteret/api/enheter/817920632").orElseThrow();
        assertEquals("https://data.brreg.no/enhetsregisteret/api/enheter/817920632", first.getUri());
        assertEquals("FYLKE 1 FYLKESKOMMUNE", first.getOrganisasjonsnavn());
        assertEquals("817920632", first.getOrganisasjonsnummer());
        assertEquals("Fylke 1", first.getFylkesnavn());
        assertEquals("50", first.getFylkesnummer());
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        FylkeOrganisasjonRepository fylkeOrganisasjonRepositorySpy = spy(this.fylkeOrganisasjonRepository);

        FylkeOrganisasjon fylkeOrganisasjon = FylkeOrganisasjon.builder()
                .uri("http://uri.no")
                .organisasjonsnavn("ORGNAVN")
                .organisasjonsnummer("ORGNR")
                .fylkesnavn("FYLKESNAVN")
                .fylkesnummer("KOMMUNENR")
                .build();
        fylkeOrganisasjonRepositorySpy.save(fylkeOrganisasjon);

        long count = fylkeOrganisasjonRepositorySpy.count();
        assertTrue(count > 0);

        when(fylkeOrganisasjonRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new FylkeOrganisasjonService(
                new LocalFylkeOrganisasjonHarvester(wiremockHost, wiremockPort),
                fylkeOrganisasjonRepositorySpy,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(mock(RDFSourceRepository.class)), mock(RDFSourceRepository.class), new HarvestMetrics(new SimpleMeterRegistry())));

        assertEquals(count, fylkeOrganisasjonRepositorySpy.count());
    }
}
