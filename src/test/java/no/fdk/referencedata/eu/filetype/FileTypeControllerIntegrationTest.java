package no.fdk.referencedata.eu.filetype;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key"
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class FileTypeControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private FileTypeRepository fileTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        FileTypeService fileTypeService = new FileTypeService(
                new LocalFileTypeHarvester("1"),
                fileTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        fileTypeService.harvestAndSave(true);
    }

    @Test
    public void test_if_get_all_filetypes_returns_valid_response() {
        FileTypes fileTypes =
                restClient.get().uri("/eu/file-types").retrieve().body(FileTypes.class);

        assertEquals(198, fileTypes.getFileTypes().size());

        FileType first = fileTypes.getFileTypes().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/file-type/7Z", first.getUri());
        assertEquals("7Z", first.getCode());
        assertEquals("application/x-7z-compressed", first.getMediaType());
    }

    @Test
    public void test_if_get_filetype_by_code_returns_valid_response() {
        FileType fileType =
                restClient.get().uri("/eu/file-types/7Z").retrieve().body(FileType.class);

        assertNotNull(fileType);
        assertEquals("http://publications.europa.eu/resource/authority/file-type/7Z", fileType.getUri());
        assertEquals("7Z", fileType.getCode());
        assertEquals("application/x-7z-compressed", fileType.getMediaType());
    }

    @Test
    public void test_if_post_file_types_fails_without_api_key() {
        assertEquals(198, fileTypeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.FILE_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = restClient.post().uri("/eu/file-types")
                .headers(h -> h.addAll(headers)).exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(198, fileTypeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.FILE_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_file_types_executes_a_force_update() {
        assertEquals(198, fileTypeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.FILE_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = restClient.post().uri("/eu/file-types")
                .headers(h -> h.addAll(headers)).exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(198, fileTypeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.FILE_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }

    @Test
    public void test_file_types_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/file-types", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(FileTypeControllerIntegrationTest.class.getClassLoader().getResource("filetypes-sparql-result.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
