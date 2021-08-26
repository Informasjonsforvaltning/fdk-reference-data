package no.fdk.referencedata.graphql;

import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import no.fdk.referencedata.eu.filetype.FileTypeRepository;
import no.fdk.referencedata.eu.filetype.FileTypeService;
import no.fdk.referencedata.eu.filetype.LocalFileTypeHarvester;
import no.fdk.referencedata.mongo.AbstractMongoDbContainerTest;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
class FileTypeQueryIntegrationTest extends AbstractMongoDbContainerTest {

    @Autowired
    private GraphQLTestTemplate template;

    @Autowired
    private FileTypeRepository fileTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @BeforeEach
    public void setup() {
        FileTypeService fileTypeService = new FileTypeService(
                new LocalFileTypeHarvester("1"),
                fileTypeRepository,
                harvestSettingsRepository);

        fileTypeService.harvestAndSave();
    }

    @Test
    void test_if_filetypes_query_returns_all_file_types() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/file-types.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://publications.europa.eu/resource/authority/file-type/7Z", response.get("$['data']['fileTypes'][0]['uri']"));
        assertEquals("7Z", response.get("$['data']['fileTypes'][0]['code']"));
        assertEquals("application/x-7z-compressed", response.get("$['data']['fileTypes'][0]['mediaType']"));
    }

    @Test
    void test_if_filetype_by_code_aac_query_returns_aac_file_type() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/file-type-by-code.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://publications.europa.eu/resource/authority/file-type/AAC", response.get("$['data']['fileTypeByCode']['uri']"));
        assertEquals("AAC", response.get("$['data']['fileTypeByCode']['code']"));
        assertEquals("audio/aac", response.get("$['data']['fileTypeByCode']['mediaType']"));
    }

    @Test
    void test_if_filetype_by_code_unknown_query_returns_null() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/file-type-by-code-unknown.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['fileTypeByCode']"));
    }

}