package no.fdk.referencedata.los;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    public void test_if_theme_has_correct_structure() {
        LosImporter losImporter = new LosImporter();

        List<LosNode> losNodes = losImporter.importFromLosSource();
        assertNotNull(losNodes);
        assertEquals(519, losNodes.size());

        LosNode akuttHjelp = losNodes.stream()
                .filter(losNode -> losNode.getUri().equals("https://psi.norge.no/los/tema/akutt-hjelp"))
                .findAny()
                .orElse(null);

        assertNotNull(akuttHjelp);
        assertTrue(akuttHjelp.getChildren().contains(URI.create("https://psi.norge.no/los/ord/krisesenter")));
        assertTrue(akuttHjelp.getChildren().contains(URI.create("https://psi.norge.no/los/ord/legevakt")));
        assertTrue(akuttHjelp.getChildren().contains(URI.create("https://psi.norge.no/los/ord/livskriser")));
        assertTrue(akuttHjelp.getChildren().contains(URI.create("https://psi.norge.no/los/ord/hjelp-ved-overgrep")));

        assertEquals(List.of(URI.create("https://psi.norge.no/los/tema/helse-og-omsorg")), akuttHjelp.getParents());
        assertTrue(akuttHjelp.isTema());
        assertEquals(List.of("helse-og-omsorg/akutt-hjelp"), akuttHjelp.getLosPaths());
        assertEquals("Akutt hjelp", akuttHjelp.getName().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertEquals("Akutt hjelp", akuttHjelp.getName().get(Language.NORWEGIAN_NYNORSK.code()));
        assertEquals("Emergency help", akuttHjelp.getName().get(Language.ENGLISH.code()));
        assertEquals(0, akuttHjelp.getSynonyms().size());
    }
}
