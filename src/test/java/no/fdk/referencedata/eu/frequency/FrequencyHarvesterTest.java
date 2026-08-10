package no.fdk.referencedata.eu.frequency;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static no.fdk.referencedata.LocalHarvestFixtures.FREQUENCIES_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class FrequencyHarvesterTest {

    @Test
    public void test_fetch_frequencies() {
        FrequencyHarvester harvester = LocalHarvesters.frequency();

        assertNotNull(harvester.getSource());
        assertEquals("frequencies-sparql-result.ttl", harvester.getSource().getFilename());

        List<Frequency> frequencies = harvester.harvest().collectList().block();
        assertNotNull(frequencies);
        assertEquals(FREQUENCIES_SIZE, frequencies.size());

        Frequency first = frequencies.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/frequency/MONTHLY_3", first.getUri());
        assertEquals("MONTHLY_3", first.getCode());
        assertEquals("three times a month", first.getLabel().get(Language.ENGLISH.code()));
    }

}
