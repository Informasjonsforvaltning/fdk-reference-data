package no.fdk.referencedata.mobility.theme;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalMobilityThemeHarvester extends MobilityThemeHarvester {

    @Override
    public Resource getSource(final String path) {
        return new ClassPathResource("mobility-themes.ttl");
    }
}
