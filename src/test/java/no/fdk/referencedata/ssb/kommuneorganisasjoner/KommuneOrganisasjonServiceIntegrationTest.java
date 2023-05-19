package no.fdk.referencedata.ssb.kommuneorganisasjoner;

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
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class KommuneOrganisasjonServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private KommuneOrganisasjonRepository kommuneOrganisasjonRepository;

    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @Test
    public void test_if_harvest_persists_kommune_organisasjoner() {
        KommuneOrganisasjonService kommuneOrganisasjonService = new KommuneOrganisasjonService(
                new LocalKommuneOrganisasjonHarvester(wiremockHost, wiremockPort),
                kommuneOrganisasjonRepository);

        kommuneOrganisasjonService.harvestAndSave();

        final KommuneOrganisasjon first = kommuneOrganisasjonRepository.findById("https://data.brreg.no/enhetsregisteret/api/enheter/817263992").orElseThrow();
        assertEquals("https://data.brreg.no/enhetsregisteret/api/enheter/817263992", first.getUri());
        assertEquals("KOMMUNE 1 KOMMUNE", first.getOrganisasjonsnavn());
        assertEquals("817263992", first.getOrganisasjonsnummer());
        assertEquals("Kommune 1", first.getKommunenavn());
        assertEquals("3811", first.getKommunenummer());
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        KommuneOrganisasjonRepository kommuneOrganisasjonRepositorySpy = spy(this.kommuneOrganisasjonRepository);

        KommuneOrganisasjon kommuneOrganisasjon = KommuneOrganisasjon.builder()
                .uri("http://uri.no")
                .organisasjonsnavn("ORGNAVN")
                .organisasjonsnummer("ORGNR")
                .kommunenavn("FYLKESNAVN")
                .kommunenummer("KOMMUNENR")
                .build();
        kommuneOrganisasjonRepositorySpy.save(kommuneOrganisasjon);


        long count = kommuneOrganisasjonRepositorySpy.count();
        assertTrue(count > 0);

        when(kommuneOrganisasjonRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        KommuneOrganisasjonService kommuneOrganisasjonService = new KommuneOrganisasjonService(
                new LocalKommuneOrganisasjonHarvester(wiremockHost, wiremockPort),
                kommuneOrganisasjonRepositorySpy);

        assertEquals(count, kommuneOrganisasjonRepositorySpy.count());
    }
}
