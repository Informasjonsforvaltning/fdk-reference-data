package no.fdk.referencedata.los;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertNull;

public class LosImporterTest {

    @Test
    public void test_import() {
        LosImporter losImporter = new LosImporter();

        List<LosNode> losNodes = losImporter.importFromLosSource();
        assertNotNull(losNodes);
        assertEquals(519, losNodes.size());

        LosNode first = losNodes.get(0);
        assertEquals("https://psi.norge.no/los/ord/abort", first.getUri());
        assertEquals("Abort", first.getName().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertEquals(false, first.isTema());
        assertEquals(List.of("helse-og-omsorg/svangerskap/abort"), first.getLosPaths());
        assertEquals(List.of(URI.create("https://psi.norge.no/los/tema/svangerskap")), first.getParents());
        assertEquals(List.of("Svangerskapsavbrudd", "Svangerskapsavbrot"), first.getSynonyms());

    }

}
