package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.iana.mediatype.LocalMediaTypeHarvester;
import no.fdk.referencedata.iana.mediatype.MediaType;
import no.fdk.referencedata.iana.mediatype.MediaTypeRepository;
import no.fdk.referencedata.iana.mediatype.MediaTypeService;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class MediaTypeQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private MediaTypeRepository mediaTypeRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

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
    void test_if_mediatypes_query_returns_all_media_types() {
        List<MediaType> result = graphQlTester.documentName("media-types")
                .execute()
                .path("$['data']['mediaTypes']")
                .entityList(MediaType.class)
                .get();

        assertEquals(1441, result.size());

        MediaType mediaType = result.get(0);

        assertEquals("https://www.iana.org/assignments/media-types/application/1d-interleaved-parityfec", mediaType.getUri());
        assertEquals("1d-interleaved-parityfec", mediaType.getName());
        assertEquals("application", mediaType.getType());
        assertEquals("1d-interleaved-parityfec", mediaType.getSubType());
    }

    @Test
    void test_if_mediatypes_by_type_text_query_returns_text_media_types() {
        List<MediaType> result = graphQlTester.documentName("media-types-by-type")
                .variable("type", "text")
                .execute()
                .path("$['data']['mediaTypesByType']")
                .entityList(MediaType.class)
                .get();

        assertEquals(1, result.size());

        MediaType mediaType = result.get(0);

        assertEquals("https://www.iana.org/assignments/media-types/text/plain", mediaType.getUri());
        assertEquals("plain", mediaType.getName());
        assertEquals("text", mediaType.getType());
        assertEquals("plain", mediaType.getSubType());
    }

    @Test
    void test_if_mediatypes_by_type_unknown_query_returns_empty_list() {
        List<MediaType> result = graphQlTester.documentName("media-types-by-type")
                .variable("type", "unknown")
                .execute()
                .path("$['data']['mediaTypesByType']")
                .entityList(MediaType.class)
                .get();

        assertTrue(result.isEmpty());
    }

    @Test
    void test_if_mediatype_by_type_and_subtype_query_returns_text_plain_media_type() {
        MediaType result = graphQlTester.documentName("media-type-by-type-and-subtype")
                .variable("type", "text")
                .variable("subType", "plain")
                .execute()
                .path("$['data']['mediaTypeByTypeAndSubType']")
                .entity(MediaType.class)
                .get();

        assertEquals("https://www.iana.org/assignments/media-types/text/plain", result.getUri());
        assertEquals("plain", result.getName());
        assertEquals("text", result.getType());
        assertEquals("plain", result.getSubType());
    }

    @Test
    void test_if_mediatype_by_type_and_subtype_unknown_query_returns_null() {
        graphQlTester.documentName("media-type-by-type-and-subtype")
                .variable("type", "text")
                .variable("subType", "unknown")
                .execute()
                .path("$['data']['mediaTypeByTypeAndSubType']")
                .valueIsNull();
    }
}
