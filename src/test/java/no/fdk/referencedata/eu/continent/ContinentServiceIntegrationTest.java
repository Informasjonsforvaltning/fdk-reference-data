package no.fdk.referencedata.eu.continent;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.eu.continent.LocalContinentHarvester.CONTINENTS_SIZE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class ContinentServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ContinentRepository continentRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_continents() {
        ContinentService continentService = new ContinentService(
                new LocalContinentHarvester(),
                continentRepository,
                rdfSourceRepository,
                new ReferenceDataWriter(rdfSourceRepository));

        continentService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        continentRepository.findAll().forEach(continent -> counter.incrementAndGet());
        assertEquals(CONTINENTS_SIZE, counter.get());

        final Continent first = continentRepository.findById("http://publications.europa.eu/resource/authority/continent/AFRICA").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/continent/AFRICA", first.getUri());
        assertEquals("AFRICA", first.getCode());
        assertEquals("Africa", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        ContinentRepository continentRepositorySpy = spy(this.continentRepository);

        Continent continent = Continent.builder()
                .uri("http://uri.no")
                .code("EUROPE")
                .label(Map.of("en", "Europe"))
                .build();
        continentRepositorySpy.save(continent);

        long count = continentRepositorySpy.count();
        assertTrue(count > 0);

        when(continentRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new ContinentService(
                new LocalContinentHarvester(),
                continentRepositorySpy,
                rdfSourceRepository,
                new ReferenceDataWriter(rdfSourceRepository));

        assertEquals(count, continentRepositorySpy.count());
    }
}
