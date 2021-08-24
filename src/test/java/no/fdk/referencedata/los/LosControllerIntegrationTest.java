package no.fdk.referencedata.los;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.iana.mediatype.*;
import no.fdk.referencedata.mongo.AbstractMongoDbContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = { "scheduling.enabled=false" })
public class LosControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test_if_get_all_los_nodes_returns_valid_response() {
        LosNodes losNodes =
                this.restTemplate.getForObject("http://localhost:" + port + "/los", LosNodes.class);

        assertEquals(519, losNodes.getLosNodes().size());

        LosNode first = losNodes.getLosNodes().get(0);
        assertEquals("https://psi.norge.no/los/ord/abort", first.getUri());
        assertEquals("Abort", first.getName().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertEquals(false, first.isTema());
        assertEquals(List.of("helse-og-omsorg/svangerskap/abort"), first.getLosPaths());
        assertEquals(List.of(URI.create("https://psi.norge.no/los/tema/svangerskap")), first.getParents());
        assertEquals(List.of("Svangerskapsavbrudd", "Svangerskapsavbrot"), first.getSynonyms());
    }

    @Test
    public void test_if_get_los_nodes_by_uris_returns_valid_response() {
        LosNodes losNodes =
                this.restTemplate.getForObject("http://localhost:" + port + "/los?uris=https%3A%2F%2Fpsi.norge.no%2Flos%2Ford%2Fabort,https%3A%2F%2Fpsi.norge.no%2Flos%2Ford%2Fadopsjon", LosNodes.class);

        assertEquals(2, losNodes.getLosNodes().size());

        LosNode first = losNodes.getLosNodes().get(0);
        assertEquals("https://psi.norge.no/los/ord/abort", first.getUri());
        assertEquals("Abort", first.getName().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertEquals(false, first.isTema());
        assertEquals(List.of("helse-og-omsorg/svangerskap/abort"), first.getLosPaths());
        assertEquals(List.of(URI.create("https://psi.norge.no/los/tema/svangerskap")), first.getParents());
        assertEquals(List.of("Svangerskapsavbrudd", "Svangerskapsavbrot"), first.getSynonyms());

        LosNode second = losNodes.getLosNodes().get(1);
        assertEquals("https://psi.norge.no/los/ord/adopsjon", second.getUri());
        assertEquals("Adopsjon", second.getName().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertEquals(false, second.isTema());
        assertEquals(List.of("familie-og-barn/foreldre-og-foresatte/adopsjon"), second.getLosPaths());
        assertEquals(List.of(URI.create("https://psi.norge.no/los/tema/foreldre-og-foresatte")), second.getParents());
        assertEquals(List.of("Stebarnsadopsjon", "Utanlandsadopsjon", "Utenlandsadopsjon"), second.getSynonyms());
    }

    @Test
    public void test_if_get_los_nodes_by_uris_with_unknown_uri_returns_valid_response() {
        LosNodes losNodes =
                this.restTemplate.getForObject("http://localhost:" + port + "/los?uris=https%3A%2F%2Fpsi.norge.no%2Flos%2Ford%2Fabort,https%3A%2F%2Fpsi.norge.no%2Flos%2Ford%2Fukjent", LosNodes.class);

        assertEquals(1, losNodes.getLosNodes().size());

        LosNode first = losNodes.getLosNodes().get(0);
        assertEquals("https://psi.norge.no/los/ord/abort", first.getUri());
        assertEquals("Abort", first.getName().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertEquals(false, first.isTema());
        assertEquals(List.of("helse-og-omsorg/svangerskap/abort"), first.getLosPaths());
        assertEquals(List.of(URI.create("https://psi.norge.no/los/tema/svangerskap")), first.getParents());
        assertEquals(List.of("Svangerskapsavbrudd", "Svangerskapsavbrot"), first.getSynonyms());
    }

}
