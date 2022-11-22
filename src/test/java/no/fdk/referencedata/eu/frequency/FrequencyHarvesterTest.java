package no.fdk.referencedata.eu.frequency;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class FrequencyHarvesterTest {

    @Test
    public void test_fetch_frequencies() {
        FrequencyHarvester harvester = new LocalFrequencyHarvester("20200923-0");

        assertNotNull(harvester.getSource());
        assertEquals("frequencies-sparql-result.ttl", harvester.getSource().getFilename());
        assertEquals("20200923-0", harvester.getVersion());

        List<Frequency> frequencies = harvester.harvest().collectList().block();
        assertNotNull(frequencies);
        assertEquals(31, frequencies.size());

        Frequency first = frequencies.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/frequency/WEEKLY_3", first.getUri());
        assertEquals("WEEKLY_3", first.getCode());
        assertEquals("three times a week", first.getLabel().get(Language.ENGLISH.code()));
    }

}
