package no.fdk.referencedata.digdir.qualitydimension;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalQualityDimensionHarvester extends QualityDimensionHarvester {
    public static final int QUALITY_DIMENSIONS_SIZE = 11;



    @Override
    public Resource getSource(final String path) {
        return new ClassPathResource("quality-dimension.ttl");
    }
}
