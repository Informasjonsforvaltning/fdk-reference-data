package no.fdk.referencedata.eu.filetype;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class FileTypeHarvesterTest {

    @Test
    public void test_fetch_filetypes() throws Exception {
        FileTypeHarvester fileTypeHarvester = new LocalFileTypeHarvester("20210512-0");

        assertNotNull(fileTypeHarvester.getSource());
        assertEquals("filetypes-sparql-result.ttl", fileTypeHarvester.getSource().getFilename());
        assertEquals("20210512-0", fileTypeHarvester.getVersion());

        List<FileType> fileTypes = fileTypeHarvester.harvest().collectList().block();
        assertNotNull(fileTypes);
        assertEquals(198, fileTypes.size());

        FileType first = fileTypes.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/file-type/BZIP2", first.getUri());
        assertEquals("BZIP2", first.getCode());
        assertEquals("application/x-bzip2", first.getMediaType());
    }

}
