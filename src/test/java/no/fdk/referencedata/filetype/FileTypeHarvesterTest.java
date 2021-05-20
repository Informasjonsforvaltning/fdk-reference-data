package no.fdk.referencedata.filetype;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FileTypeHarvesterTest {

    @Test
    public void test_fetch_filetypes() throws Exception {
        FileTypeHarvester fileTypeHarvester = new LocalFileTypeHarvester("20210512-0");

        assertNotNull(fileTypeHarvester.getFileTypesSource());
        assertEquals("filetypes-skos-ap-act.rdf", fileTypeHarvester.getFileTypesSource().getFilename());
        assertEquals("20210512-0", fileTypeHarvester.getVersion());

        List<FileType> fileTypes = fileTypeHarvester.harvestFileTypes().collectList().block();
        assertNotNull(fileTypes);
        assertEquals(15, fileTypes.size());

        FileType first = fileTypes.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/file-type/BIN", first.getUri());
        assertEquals("BIN", first.getCode());
        assertEquals("application/octet-stream", first.getMediaType());
    }

}
