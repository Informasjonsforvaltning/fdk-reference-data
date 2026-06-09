package no.fdk.referencedata.eu.country;

import no.fdk.referencedata.eu.country.CountryWriter;
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

import static no.fdk.referencedata.eu.country.LocalCountryHarvester.COUNTRIES_SIZE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class CountryServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private CountryRepository countryRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_countries() {
        CountryService countryService = new CountryService(
                new LocalCountryHarvester(),
                countryRepository,
                rdfSourceRepository,
                new CountryWriter(countryRepository, rdfSourceRepository));

        countryService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        countryRepository.findAll().forEach(country -> counter.incrementAndGet());
        assertEquals(COUNTRIES_SIZE, counter.get());

        final Country first = countryRepository.findById("http://publications.europa.eu/resource/authority/country/DEU").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/country/DEU", first.getUri());
        assertEquals("DEU", first.getCode());
        assertEquals("Germany", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        CountryRepository countryRepositorySpy = spy(this.countryRepository);

        Country country = Country.builder()
                .uri("http://uri.no")
                .code("NOR")
                .label(Map.of("en", "Norway"))
                .build();
        countryRepositorySpy.save(country);

        long count = countryRepositorySpy.count();
        assertTrue(count > 0);

        when(countryRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new CountryService(
                new LocalCountryHarvester(),
                countryRepositorySpy,
                rdfSourceRepository,
                new CountryWriter(countryRepository, rdfSourceRepository));

        assertEquals(count, countryRepositorySpy.count());
    }
}
