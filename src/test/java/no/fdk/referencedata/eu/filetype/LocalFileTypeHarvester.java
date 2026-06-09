package no.fdk.referencedata.eu.filetype;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalFileTypeHarvester extends FileTypeHarvester {

    @Override
    public Resource getSource() {
        return new ClassPathResource("filetypes-sparql-result.ttl");
    }
}
