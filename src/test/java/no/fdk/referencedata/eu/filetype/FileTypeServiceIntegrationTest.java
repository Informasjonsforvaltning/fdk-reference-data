package no.fdk.referencedata.eu.filetype;

import no.fdk.referencedata.mongo.AbstractMongoDbContainerTest;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.settings.Settings.FILE_TYPE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = { "scheduling.enabled=false" })
public class FileTypeServiceIntegrationTest extends AbstractMongoDbContainerTest {

    @Autowired
    private FileTypeRepository fileTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Test
    public void test_if_harvest_persists_filetypes() {
        FileTypeService fileTypeService = new FileTypeService(
                new LocalFileTypeHarvester("20210512-0"),
                fileTypeRepository,
                harvestSettingsRepository);

        fileTypeService.harvestAndSaveFileTypes();

        final AtomicInteger counter = new AtomicInteger();
        fileTypeRepository.findAll().forEach(fileType -> counter.incrementAndGet());
        assertEquals(15, counter.get());

        final FileType first = fileTypeRepository.findById("http://publications.europa.eu/resource/authority/file-type/7Z").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/file-type/7Z", first.getUri());
        assertEquals("7Z", first.getCode());
        assertEquals("application/x-7z-compressed", first.getMediaType());
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        FileTypeService fileTypeService = new FileTypeService(
                new LocalFileTypeHarvester("20210512-6"),
                fileTypeRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        fileTypeService.harvestAndSaveFileTypes();

        HarvestSettings settings =
                harvestSettingsRepository.findById(FILE_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20210512-6", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        fileTypeService = new FileTypeService(
                new LocalFileTypeHarvester("20210513-0"),
                fileTypeRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        fileTypeService.harvestAndSaveFileTypes();

        settings =
                harvestSettingsRepository.findById(FILE_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20210513-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        fileTypeService = new FileTypeService(
                new LocalFileTypeHarvester("20210512-6"),
                fileTypeRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        fileTypeService.harvestAndSaveFileTypes();

        settings =
                harvestSettingsRepository.findById(FILE_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20210513-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        FileTypeRepository fileTypeRepositorySpy = spy(fileTypeRepository);

        fileTypeRepositorySpy.save(FileType.builder()
                .uri("http://uri.no")
                .code("FIL")
                .mediaType("text/fil")
                .build());


        long count = fileTypeRepositorySpy.count();
        assertTrue(count > 0);

        when(fileTypeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        FileTypeService fileTypeService = new FileTypeService(
                new LocalFileTypeHarvester("20210514-2"),
                fileTypeRepositorySpy,
                harvestSettingsRepository);

        assertEquals(count, fileTypeRepositorySpy.count());
    }
}
