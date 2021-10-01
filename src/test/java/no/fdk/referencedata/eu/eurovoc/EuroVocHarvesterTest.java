package no.fdk.referencedata.eu.eurovoc;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class EuroVocHarvesterTest {

    @Test
    public void test_fetch_EuroVoc() {
        EuroVocHarvester euroVocHarvester = new LocalEuroVocHarvester("20200923-0");

        assertNotNull(euroVocHarvester.getSource());
        assertEquals("eurovoc_in_skos_core_concepts.zip", euroVocHarvester.getSource().getFilename());
        assertEquals("20200923-0", euroVocHarvester.getVersion());

        List<EuroVoc> euroVocList = euroVocHarvester.harvest().collectList().block();
        assertNotNull(euroVocList);
        assertEquals(1916, euroVocList.size());

        EuroVoc first = euroVocList.get(0);
        assertEquals("http://eurovoc.europa.eu/7353", first.getUri());
        assertEquals("7353", first.getCode());
        assertEquals("territorial enclave", first.getLabel().get(Language.ENGLISH.code()));
    }

}
