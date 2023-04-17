package no.fdk.referencedata.los;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class LosServiceTest extends AbstractContainerTest {

    @Autowired
    private LosRepository losRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_get_all_returns_all_los_nodes() {
        LosService losService = new LosService(new LocalLosImporter(), losRepository, rdfSourceRepository);
        losService.importLosNodes();

        List<LosNode> losNodeList = losService.getAll();
        assertEquals(519, losNodeList.size());

        final LosNode first = losNodeList.get(0);
        assertEquals("https://psi.norge.no/los/ord/abort", first.getUri());
        assertEquals("Abort", first.getName().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertFalse(first.isTheme());
        assertEquals(List.of("helse-og-omsorg/svangerskap/abort"), first.getLosPaths());
        assertEquals(List.of(URI.create("https://psi.norge.no/los/tema/svangerskap")), first.getParents());
        assertEquals(List.of("Svangerskapsavbrudd", "Svangerskapsavbrot"), first.getSynonyms());
    }

    @Test
    public void test_if_get_los_nodes_by_uris_returns_correct_los_nodes() {
        LosService losService = new LosService(new LocalLosImporter(), losRepository, rdfSourceRepository);
        losService.importLosNodes();

        List<LosNode> losNodeList = losService.getByURIs(List.of(
                "https://psi.norge.no/los/ord/festival",
                "https://psi.norge.no/los/tema/politikk-og-valg",
                "https://psi.norge.no/los/tema/var-og-klima"));

        assertEquals(3, losNodeList.size());

        final LosNode first = losNodeList.get(0);
        assertEquals("https://psi.norge.no/los/ord/festival", first.getUri());
        assertEquals("Festival", first.getName().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertFalse(first.isTheme());
        assertEquals(List.of("kultur-idrett-og-fritid/kultur/festival"), first.getLosPaths());
        assertEquals(List.of(URI.create("https://psi.norge.no/los/tema/kultur")), first.getParents());
        assertEquals(List.of("Billettbestilling", "Festivalpass"), first.getSynonyms());

        final LosNode second = losNodeList.get(1);
        assertEquals("https://psi.norge.no/los/tema/politikk-og-valg", second.getUri());
        assertEquals("Politikk og valg", second.getName().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertTrue(second.isTheme());
        assertEquals(List.of("demokrati-og-innbyggerrettigheter/politikk-og-valg"), second.getLosPaths());
        assertEquals(List.of(URI.create("https://psi.norge.no/los/tema/demokrati-og-innbyggerrettigheter")), second.getParents());
        assertEquals(emptyList(), second.getSynonyms());

        final LosNode third = losNodeList.get(2);
        assertEquals("https://psi.norge.no/los/tema/var-og-klima", third.getUri());
        assertEquals("Vær og klima", third.getName().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertTrue(third.isTheme());
        assertEquals(List.of("natur-klima-og-miljo/var-og-klima"), third.getLosPaths());
        assertEquals(List.of(URI.create("https://psi.norge.no/los/tema/natur-klima-og-miljo")), third.getParents());
        assertEquals(emptyList(), third.getSynonyms());
    }

}
