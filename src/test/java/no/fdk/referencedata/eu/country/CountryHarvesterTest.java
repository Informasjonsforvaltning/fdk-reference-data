package no.fdk.referencedata.eu.country;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.Comparator;
import java.util.List;

import static no.fdk.referencedata.eu.country.LocalCountryHarvester.COUNTRIES_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class CountryHarvesterTest {

    @Test
    public void test_fetch_countries() {
        CountryHarvester harvester = new LocalCountryHarvester();

        assertNotNull(harvester.getSource());
        assertEquals("country-sparql-result.ttl", harvester.getSource().getFilename());

        List<Country> countries = harvester.harvest().collectList().block();
        assertNotNull(countries);
        assertEquals(COUNTRIES_SIZE, countries.size());

        countries.sort(Comparator.comparing(Country::getUri));
        Country first = countries.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/country/DEU", first.getUri());
        assertEquals("DEU", first.getCode());
        assertEquals("Germany", first.getLabel().get(Language.ENGLISH.code()));
    }
}
