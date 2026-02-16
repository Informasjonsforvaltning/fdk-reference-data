package no.fdk.referencedata.digdir.qualitydimension;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalQualityDimensionHarvester extends QualityDimensionHarvester {
    private final String version;
    public static final int QUALITY_DIMENSIONS_SIZE = 11;

    public LocalQualityDimensionHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource(final String path) {
        return new ClassPathResource("quality-dimension.ttl");
    }
}
