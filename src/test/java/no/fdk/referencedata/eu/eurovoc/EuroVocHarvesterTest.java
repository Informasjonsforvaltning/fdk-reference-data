package no.fdk.referencedata.eu.eurovoc;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static no.fdk.referencedata.eu.eurovoc.LocalEuroVocHarvester.EUROVOCS_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class EuroVocHarvesterTest {

    @Test
    public void test_fetch_EuroVoc() {
        EuroVocHarvester euroVocHarvester = new LocalEuroVocHarvester("20200923-0");

        assertNotNull(euroVocHarvester.getSource());
        assertEquals("eurovoc-sparql-result.ttl", euroVocHarvester.getSource().getFilename());
        assertEquals("20200923-0", euroVocHarvester.getVersion());

        List<EuroVoc> euroVocList = euroVocHarvester.harvest().collectList().block();
        assertNotNull(euroVocList);
        assertEquals(EUROVOCS_SIZE, euroVocList.size());

        EuroVoc first = euroVocList.get(0);
        assertEquals("http://eurovoc.europa.eu/5560", first.getUri());
        assertEquals("5560", first.getCode());
        assertEquals("organised crime", first.getLabel().get(Language.ENGLISH.code()));
    }

}
