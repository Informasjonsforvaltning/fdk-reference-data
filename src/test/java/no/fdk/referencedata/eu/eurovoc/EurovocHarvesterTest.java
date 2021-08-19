package no.fdk.referencedata.eu.eurovoc;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EurovocHarvesterTest {

    @Test
    public void test_fetch_eurovoc() throws Exception {
        EurovocHarvester eurovocHarvester = new LocalEurovocHarvester("20200923-0");

        assertNotNull(eurovocHarvester.getSource());
        assertEquals("eurovoc_in_skos_core_concepts.zip", eurovocHarvester.getSource().getFilename());
        assertEquals("20200923-0", eurovocHarvester.getVersion());

        List<Eurovoc> eurovocList = eurovocHarvester.harvest().collectList().block();
        assertNotNull(eurovocList);
        assertEquals(7322, eurovocList.size());

        Eurovoc first = eurovocList.get(0);
        assertEquals("http://eurovoc.europa.eu/7353", first.getUri());
        assertEquals("7353", first.getCode());
        assertEquals("territorial enclave", first.getLabel().get(Language.ENGLISH.code()));
    }

}
