package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.los.LocalLosImporter;
import no.fdk.referencedata.los.LosNode;
import no.fdk.referencedata.los.LosRepository;
import no.fdk.referencedata.los.LosService;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class LosQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private LosRepository losRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @BeforeEach
    public void setup() {
        LosService losService = new LosService(
                new LocalLosImporter(),
                losRepository,
                rdfSourceRepository);

        losService.importLosNodes();
    }

    @Test
    void test_if_los_themes_and_words_query_returns_all_themes_and_words_as_los_nodes() {
        List<LosNode> result = graphQlTester.documentName("los-themes-and-words")
                .execute()
                .path("$['data']['losThemesAndWords']")
                .entityList(LosNode.class)
                .get();

        assertEquals(519, result.size());

        assertEquals("https://psi.norge.no/los/ord/abort", result.get(0).getUri());
        assertEquals("Abort", result.get(0).getName().get("nb"));
        assertEquals("Abort", result.get(0).getName().get("nn"));
        assertEquals("Abortion", result.get(0).getName().get("en"));
    }

    @Test
    void test_if_los_themes_and_words_by_uris_query_returns_abortion_los_node() {
        List<LosNode> result = graphQlTester.documentName("los-themes-and-words-by-uris")
                .variable("uris", List.of("https://psi.norge.no/los/ord/abort"))
                .execute()
                .path("$['data']['losThemesAndWords']")
                .entityList(LosNode.class)
                .get();

        assertEquals(1, result.size());

        assertEquals("https://psi.norge.no/los/ord/abort", result.get(0).getUri());
    }

    @Test
    void test_if_los_themes_and_words_by_uris_unknown_query_returns_empty_list() {
            List<LosNode> result = graphQlTester.documentName("los-themes-and-words-by-uris")
                .variable("uris", List.of("https://psi.norge.no/los/ord/unknown"))
                .execute()
                .path("$['data']['losThemesAndWords']")
                .entityList(LosNode.class)
                .get();

            assertTrue(result.isEmpty());
    }
}
