package no.fdk.referencedata.eu.datatheme;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalDataThemeHarvester extends DataThemeHarvester {
    private final String version;

    public LocalDataThemeHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource() {
        return new ClassPathResource("data-theme-skos-ap-act.rdf");
    }
}
