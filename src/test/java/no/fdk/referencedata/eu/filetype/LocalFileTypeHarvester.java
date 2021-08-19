package no.fdk.referencedata.eu.filetype;

import no.fdk.referencedata.eu.filetype.eu.EUFileTypeHarvester;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalFileTypeHarvester extends EUFileTypeHarvester {
    private final String version;

    public LocalFileTypeHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getFileTypesSource() {
        return new ClassPathResource("filetypes-skos-ap-act.rdf");
    }
}
