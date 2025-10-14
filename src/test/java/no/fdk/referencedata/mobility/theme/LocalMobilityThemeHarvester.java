package no.fdk.referencedata.mobility.theme;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalMobilityThemeHarvester extends MobilityThemeHarvester {
    private final String version;

    public LocalMobilityThemeHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource(final String path) {
        return new ClassPathResource("mobility-themes.ttl");
    }
}
