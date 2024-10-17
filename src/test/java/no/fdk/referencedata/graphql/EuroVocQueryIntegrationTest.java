package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.eu.eurovoc.EuroVoc;
import no.fdk.referencedata.eu.eurovoc.EuroVocRepository;
import no.fdk.referencedata.eu.eurovoc.EuroVocService;
import no.fdk.referencedata.eu.eurovoc.LocalEuroVocHarvester;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class EuroVocQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private EuroVocRepository euroVocRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @BeforeEach
    public void setup() {
        EuroVocService EuroVocService = new EuroVocService(
                new LocalEuroVocHarvester("1"),
                euroVocRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        EuroVocService.harvestAndSave(false);
    }

    @Test
    void test_if_eurovocs_query_returns_all_eurovocs() throws URISyntaxException {
            List<EuroVoc> result = graphQlTester.documentName("eurovocs")
                    .execute()
                    .path("$['data']['euroVocs']")
                    .entityList(EuroVoc.class)
                    .get();

            Assertions.assertEquals(7403, result.size());

            EuroVoc euroVoc = result.get(0);

        assertEquals("http://eurovoc.europa.eu/1", euroVoc.getUri());
        assertEquals("1", euroVoc.getCode());
            assertEquals("Århus (county)", euroVoc.getLabel().get("en"));
        assertTrue(euroVoc.getParents().contains(new URI(("http://eurovoc.europa.eu/337"))));
    }

    @Test
    void test_if_eurovoc_by_code_5548_query_returns_interinstitutional_cooperation_eu() {
            EuroVoc result = graphQlTester.documentName("eurovoc-by-code")
                    .variable("code", "337")
                    .execute()
                    .path("$['data']['euroVocByCode']")
                    .entity(EuroVoc.class)
                    .get();

        assertEquals("http://eurovoc.europa.eu/337", result.getUri());
        assertEquals("337", result.getCode());
        assertEquals("regions of Denmark", result.getLabel().get("en"));
    }

    @Test
    void test_if_eurovoc_by_code_unknown_query_returns_null() {
            graphQlTester.documentName("eurovoc-by-code")
                    .variable("code", "unknown")
                    .execute()
                    .path("$['data']['euroVocByCode']")
                    .valueIsNull();
    }

}
