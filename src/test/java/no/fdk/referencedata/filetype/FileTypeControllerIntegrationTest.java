package no.fdk.referencedata.filetype;

import no.fdk.referencedata.redis.AbstractRedisContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FileTypeControllerIntegrationTest extends AbstractRedisContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private FileTypeRepository fileTypeRepository;

    @Autowired
    private FileTypeSettingsRepository fileTypeSettingsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        FileTypeService fileTypeService = new FileTypeService(
                new LocalFileTypeHarvester("1"),
                fileTypeRepository,
                fileTypeSettingsRepository);

        fileTypeService.harvestAndSaveFileTypes();
    }

    @Test
    public void test_if_get_all_filetypes_returns_valid_response() {
        FileTypes fileTypes =
                this.restTemplate.getForObject("http://localhost:" + port + "/file-types", FileTypes.class);

        assertEquals(15, fileTypes.getFileTypes().size());

        FileType first = fileTypes.getFileTypes().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/file-type/7Z", first.getUri());
        assertEquals("7Z", first.getCode());
        assertEquals("application/x-7z-compressed", first.getMediaType());
    }

    @Test
    public void test_if_get_single_filetype_returns_valid_response() {
        FileType fileType =
                this.restTemplate.getForObject("http://localhost:" + port + "/file-types/http%3A%2F%2Fpublications.europa.eu%2Fresource%2Fauthority%2Ffile-type%2F7Z", FileType.class);

        assertNotNull(fileType);
        assertEquals("http://publications.europa.eu/resource/authority/file-type/7Z", fileType.getUri());
        assertEquals("7Z", fileType.getCode());
        assertEquals("application/x-7z-compressed", fileType.getMediaType());
    }
}