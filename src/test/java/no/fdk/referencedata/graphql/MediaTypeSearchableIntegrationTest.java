package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.iana.mediatype.LocalMediaTypeHarvester;
import no.fdk.referencedata.iana.mediatype.MediaTypeRepository;
import no.fdk.referencedata.iana.mediatype.MediaTypeService;
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

import static no.fdk.referencedata.search.SearchAlternative.IANA_MEDIA_TYPES;
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
class MediaTypeSearchableIntegrationTest extends AbstractContainerTest {

    @Autowired
    private MediaTypeRepository mediaTypeRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private GraphQlTester graphQlTester;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MediaTypeService mediaTypeService = new MediaTypeService(
                new LocalMediaTypeHarvester(),
                mediaTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        mediaTypeService.harvestAndSave();
    }

    @Test
    void test_if_search_query_returns_iana_media_type_hit() {
        SearchRequest req = SearchRequest.builder().query("OCTET-STREAM").types(List.of(IANA_MEDIA_TYPES)).build();
        List<SearchHit> result = graphQlTester.documentName("search")
                .variable("req", objectMapper.convertValue(req, Map.class))
                .execute()
                .path("$['data']['search']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(1, result.size());

        SearchHit hit = result.get(0);

        assertEquals("https://www.iana.org/assignments/media-types/application/octet-stream", hit.getUri());
        assertEquals("application/octet-stream", hit.getCode());
        assertEquals("octet-stream", hit.getLabel().get("en"));
        assertEquals(IANA_MEDIA_TYPES, hit.getType());
    }

    @Test
    void test_if_that_hits_that_starts_with_search_query_is_prioritized_in_sort() {
        SearchRequest req = SearchRequest.builder().query("json").types(List.of(IANA_MEDIA_TYPES)).build();
        List<SearchHit> result = graphQlTester.documentName("search")
                .variable("req", objectMapper.convertValue(req, Map.class))
                .execute()
                .path("$['data']['search']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(115, result.size());

        assertEquals("https://www.iana.org/assignments/media-types/application/json", result.get(0).getUri());
        assertEquals("application/json", result.get(0).getCode());
        assertEquals("json", result.get(0).getLabel().get("en"));
        assertEquals(IANA_MEDIA_TYPES, result.get(0).getType());

        assertEquals("https://www.iana.org/assignments/media-types/application/json-patch+json", result.get(1).getUri());
        assertEquals("application/json-patch+json", result.get(1).getCode());
        assertEquals("json-patch+json", result.get(1).getLabel().get("en"));
        assertEquals(IANA_MEDIA_TYPES, result.get(1).getType());

        assertEquals("https://www.iana.org/assignments/media-types/application/json-seq", result.get(2).getUri());
        assertEquals("application/json-seq", result.get(2).getCode());
        assertEquals("json-seq", result.get(2).getLabel().get("en"));
        assertEquals(IANA_MEDIA_TYPES, result.get(2).getType());
    }

    @Test
    void test_if_find_by_uris_query_returns_combined_location_hits() {
        List<String> expectedURIs = List.of(
                "https://www.iana.org/assignments/media-types/application/json",
                "https://www.iana.org/assignments/media-types/application/xml"
        );
        FindByURIsRequest req = FindByURIsRequest.builder().uris(expectedURIs).types(List.of(IANA_MEDIA_TYPES)).build();

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
