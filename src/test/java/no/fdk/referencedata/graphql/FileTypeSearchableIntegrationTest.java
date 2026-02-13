package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.eu.filetype.FileTypeRepository;
import no.fdk.referencedata.eu.filetype.FileTypeService;
import no.fdk.referencedata.eu.filetype.LocalFileTypeHarvester;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.search.FindByURIsRequest;
import no.fdk.referencedata.search.SearchHit;
import no.fdk.referencedata.search.SearchRequest;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static no.fdk.referencedata.search.SearchAlternative.EU_FILE_TYPES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class FileTypeSearchableIntegrationTest extends AbstractContainerTest {

    @Autowired
    private FileTypeRepository fileTypeRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private GraphQlTester graphQlTester;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        FileTypeService fileTypeService = new FileTypeService(
                new LocalFileTypeHarvester("1"),
                fileTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        fileTypeService.harvestAndSave(false);
    }

    @Test
    void test_if_search_query_returns_iana_media_type_hit() {
        SearchRequest req = SearchRequest.builder().query("CSV").types(List.of(EU_FILE_TYPES)).build();
        List<SearchHit> result = graphQlTester.documentName("search")
                .variable("req", objectMapper.convertValue(req, Map.class))
                .execute()
                .path("$['data']['search']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(1, result.size());

        SearchHit hit = result.get(0);

        assertEquals("http://publications.europa.eu/resource/authority/file-type/CSV", hit.getUri());
        assertEquals("CSV", hit.getCode());
        assertEquals("CSV", hit.getLabel().get("en"));
        assertEquals(EU_FILE_TYPES, hit.getType());
    }

    @Test
    void test_if_that_hits_that_starts_with_search_query_is_prioritized_in_sort() {
        SearchRequest req = SearchRequest.builder().query("json").types(List.of(EU_FILE_TYPES)).build();
        List<SearchHit> result = graphQlTester.documentName("search")
                .variable("req", objectMapper.convertValue(req, Map.class))
                .execute()
                .path("$['data']['search']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(3, result.size());

        assertEquals("http://publications.europa.eu/resource/authority/file-type/JSON", result.get(0).getUri());
        assertEquals("JSON", result.get(0).getCode());
        assertEquals("JSON", result.get(0).getLabel().get("en"));
        assertEquals(EU_FILE_TYPES, result.get(0).getType());

        assertEquals("http://publications.europa.eu/resource/authority/file-type/JSON_LD", result.get(1).getUri());
        assertEquals("JSON_LD", result.get(1).getCode());
        assertEquals("JSON_LD", result.get(1).getLabel().get("en"));
        assertEquals(EU_FILE_TYPES, result.get(1).getType());

        assertEquals("http://publications.europa.eu/resource/authority/file-type/GEOJSON", result.get(2).getUri());
        assertEquals("GEOJSON", result.get(2).getCode());
        assertEquals("GEOJSON", result.get(2).getLabel().get("en"));
        assertEquals(EU_FILE_TYPES, result.get(2).getType());
    }

    @Test
    void test_if_find_by_uris_query_returns_combined_location_hits() {
        List<String> expectedURIs = List.of(
                "http://publications.europa.eu/resource/authority/file-type/CSV",
                "http://publications.europa.eu/resource/authority/file-type/JSON"
        );
        FindByURIsRequest req = FindByURIsRequest.builder().uris(expectedURIs).types(List.of(EU_FILE_TYPES)).build();

        List<SearchHit> actual = graphQlTester.documentName("find-by-uris")
                .variable("req", objectMapper.convertValue(req, Map.class))
                .execute()
                .path("$['data']['findByURIs']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(2, actual.size());

        List<String> actualURIs = Stream.of(
                actual.get(0).getUri(),
                actual.get(1).getUri()
        ).sorted().toList();

        assertEquals(expectedURIs, actualURIs);
    }
}
