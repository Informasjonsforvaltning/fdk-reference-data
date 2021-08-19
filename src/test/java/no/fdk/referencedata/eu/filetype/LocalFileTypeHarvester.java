package no.fdk.referencedata.eu.filetype;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalFileTypeHarvester extends FileTypeHarvester {
    private final String version;

    public LocalFileTypeHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource() {
        return new ClassPathResource("filetypes-skos-ap-act.rdf");
    }
}
