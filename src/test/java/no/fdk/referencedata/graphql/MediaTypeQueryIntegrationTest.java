package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.jayway.jsonpath.PathNotFoundException;
import no.fdk.referencedata.iana.mediatype.LocalMediaTypeHarvester;
import no.fdk.referencedata.iana.mediatype.MediaTypeRepository;
import no.fdk.referencedata.iana.mediatype.MediaTypeService;
import no.fdk.referencedata.mongo.AbstractMongoDbContainerTest;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
class MediaTypeQueryIntegrationTest extends AbstractMongoDbContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private GraphQLTestTemplate template;

    @Autowired
    private MediaTypeRepository mediaTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @BeforeEach
    public void setup() {
        MediaTypeService mediaTypeService = new MediaTypeService(
                new LocalMediaTypeHarvester(),
                mediaTypeRepository,
                harvestSettingsRepository);

        mediaTypeService.harvestAndSave();
    }

    @Test
    void test_if_mediatypes_query_returns_all_media_types() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/media-types.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://www.iana.org/assignments/media-types/application/1d-interleaved-parityfec", response.get("$['data']['mediaTypes'][0]['uri']"));
        assertEquals("1d-interleaved-parityfec", response.get("$['data']['mediaTypes'][0]['name']"));
        assertEquals("application", response.get("$['data']['mediaTypes'][0]['type']"));
        assertEquals("1d-interleaved-parityfec", response.get("$['data']['mediaTypes'][0]['subType']"));
        assertNotNull(response.get("$['data']['mediaTypes'][1440]['name']"));
        assertThrows(PathNotFoundException.class, () -> response.get("$['data']['mediaTypes'][1441]"));
    }

    @Test
    void test_if_mediatypes_by_type_text_query_returns_text_media_types() throws IOException {
        GraphQLResponse response = template.perform("graphql/media-types-by-type.graphql",
                mapper.valueToTree(Map.of("type", "text")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://www.iana.org/assignments/media-types/text/plain", response.get("$['data']['mediaTypesByType'][0]['uri']"));
        assertEquals("plain", response.get("$['data']['mediaTypesByType'][0]['name']"));
        assertEquals("text", response.get("$['data']['mediaTypesByType'][0]['type']"));
        assertEquals("plain", response.get("$['data']['mediaTypesByType'][0]['subType']"));
    }

    @Test
    void test_if_mediatypes_by_type_unknown_query_returns_empty_list() throws IOException {
        GraphQLResponse response = template.perform("graphql/media-types-by-type.graphql",
                mapper.valueToTree(Map.of("type", "unknown")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertThrows(PathNotFoundException.class, () -> response.get("$['data']['mediaTypesByType'][0]"));
    }

    @Test
    void test_if_mediatype_by_type_and_subtype_query_returns_text_plain_media_type() throws IOException {
        GraphQLResponse response = template.perform("graphql/media-type-by-type-and-subtype.graphql",
                mapper.valueToTree(Map.of("type", "text", "subType", "plain")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://www.iana.org/assignments/media-types/text/plain", response.get("$['data']['mediaTypeByTypeAndSubType']['uri']"));
        assertThrows(PathNotFoundException.class, () -> response.get("$['data']['mediaTypeByTypeAndSubType']['name']"));
    }

    @Test
    void test_if_mediatype_by_type_and_subtype_unknown_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/media-type-by-type-and-subtype.graphql",
                mapper.valueToTree(Map.of("type", "text", "subType", "unknown")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['mediaTypeByTypeAndSubType']"));
    }
}
