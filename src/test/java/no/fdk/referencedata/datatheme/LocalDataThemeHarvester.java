package no.fdk.referencedata.datatheme;

import no.fdk.referencedata.datatheme.eu.EUDataThemeHarvester;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalDataThemeHarvester extends EUDataThemeHarvester {
    private final String version;

    public LocalDataThemeHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getDataThemesSource() {
        return new ClassPathResource("data-theme-skos-ap-act.rdf");
    }
}
