package no.fdk.referencedata.los;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;

public class LosServiceTest {

    @Test
    public void test_if_get_all_returns_all_los_nodes() {
        LosService losService = new LosService(new LosImporter());
        losService.importLosNodes();

        List<LosNode> losNodeList = losService.getAll();
        assertEquals(519, losNodeList.size());

        final LosNode first = losNodeList.get(0);
        assertEquals("https://psi.norge.no/los/ord/abort", first.getUri());
        assertEquals("Abort", first.getName().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertEquals(false, first.isTema());
        assertEquals(List.of("helse-og-omsorg/svangerskap/abort"), first.getLosPaths());
        assertEquals(List.of(URI.create("https://psi.norge.no/los/tema/svangerskap")), first.getParents());
        assertEquals(List.of("Svangerskapsavbrudd", "Svangerskapsavbrot"), first.getSynonyms());
    }

    @Test
    public void test_if_get_los_nodes_by_uris_returns_correct_los_nodes() {
        LosService losService = new LosService(new LosImporter());
        losService.importLosNodes();

        List<LosNode> losNodeList = losService.getByURIs(List.of(
                "https://psi.norge.no/los/ord/festival",
                "https://psi.norge.no/los/tema/politikk-og-valg",
                "https://psi.norge.no/los/tema/var-og-klima"));

        assertEquals(3, losNodeList.size());

        final LosNode first = losNodeList.get(0);
        assertEquals("https://psi.norge.no/los/ord/festival", first.getUri());
        assertEquals("Festival", first.getName().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertEquals(false, first.isTema());
        assertEquals(List.of("kultur-idrett-og-fritid/kultur/festival"), first.getLosPaths());
        assertEquals(List.of(URI.create("https://psi.norge.no/los/tema/kultur")), first.getParents());
        assertEquals(List.of("Billettbestilling", "Festivalpass"), first.getSynonyms());

        final LosNode second = losNodeList.get(1);
        assertEquals("https://psi.norge.no/los/tema/politikk-og-valg", second.getUri());
        assertEquals("Politikk og valg", second.getName().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertEquals(true, second.isTema());
        assertEquals(List.of("demokrati-og-innbyggerrettigheter/politikk-og-valg"), second.getLosPaths());
        assertEquals(List.of(URI.create("https://psi.norge.no/los/tema/demokrati-og-innbyggerrettigheter")), second.getParents());
        assertEquals(emptyList(), second.getSynonyms());

        final LosNode third = losNodeList.get(2);
        assertEquals("https://psi.norge.no/los/tema/var-og-klima", third.getUri());
        assertEquals("Vær og klima", third.getName().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertEquals(true, third.isTema());
        assertEquals(List.of("natur-klima-og-miljo/var-og-klima"), third.getLosPaths());
        assertEquals(List.of(URI.create("https://psi.norge.no/los/tema/natur-klima-og-miljo")), third.getParents());
        assertEquals(emptyList(), third.getSynonyms());
    }

}
