package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.eu.filetype.FileType;
import no.fdk.referencedata.eu.filetype.FileTypeRepository;
import no.fdk.referencedata.eu.filetype.FileTypeService;
import no.fdk.referencedata.eu.filetype.LocalFileTypeHarvester;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.Assertions;
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
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class FileTypeQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private FileTypeRepository fileTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

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
    void test_if_filetypes_query_returns_all_file_types() {
        List<FileType> result = graphQlTester.documentName("file-types")
                .execute()
                .path("$['data']['fileTypes']")
                .entityList(FileType.class)
                .get();

        Assertions.assertEquals(198, result.size());

        FileType fileType = result.get(0);

        assertEquals("http://publications.europa.eu/resource/authority/file-type/7Z", fileType.getUri());
        assertEquals("7Z", fileType.getCode());
        assertEquals("application/x-7z-compressed", fileType.getMediaType());
    }

    @Test
    void test_if_filetype_by_code_aac_query_returns_aac_file_type() {
        FileType result = graphQlTester.documentName("file-type-by-code")
                .variable("code", "AAC")
                .execute()
                .path("$['data']['fileTypeByCode']")
                .entity(FileType.class)
                .get();

        assertEquals("http://publications.europa.eu/resource/authority/file-type/AAC", result.getUri());
        assertEquals("AAC", result.getCode());
        assertEquals("audio/aac", result.getMediaType());
    }

    @Test
    void test_if_filetype_by_code_unknown_query_returns_null() {
        graphQlTester.documentName("file-type-by-code")
                .variable("code", "unknown")
                .execute()
                .path("$['data']['fileTypeByCode']")
                .valueIsNull();
    }

}
