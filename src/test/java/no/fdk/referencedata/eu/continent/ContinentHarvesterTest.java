package no.fdk.referencedata.eu.continent;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.Comparator;
import java.util.List;

import static no.fdk.referencedata.eu.continent.LocalContinentHarvester.CONTINENTS_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class ContinentHarvesterTest {

    @Test
    public void test_fetch_continents() {
        ContinentHarvester harvester = new LocalContinentHarvester();

        assertNotNull(harvester.getSource());
        assertEquals("continent-sparql-result.ttl", harvester.getSource().getFilename());

        List<Continent> continents = harvester.harvest().collectList().block();
        assertNotNull(continents);
        assertEquals(CONTINENTS_SIZE, continents.size());

        continents.sort(Comparator.comparing(Continent::getUri));
        Continent first = continents.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/continent/AFRICA", first.getUri());
        assertEquals("AFRICA", first.getCode());
        assertEquals("Africa", first.getLabel().get(Language.ENGLISH.code()));
    }
}
