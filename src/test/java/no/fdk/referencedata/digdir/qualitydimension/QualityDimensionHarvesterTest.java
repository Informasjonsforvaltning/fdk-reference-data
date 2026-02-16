package no.fdk.referencedata.digdir.qualitydimension;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static no.fdk.referencedata.digdir.qualitydimension.LocalQualityDimensionHarvester.QUALITY_DIMENSIONS_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class QualityDimensionHarvesterTest {

    @Test
    public void test_fetch_quality_dimensions() {
        QualityDimensionHarvester harvester = new LocalQualityDimensionHarvester("2023-01-30");

        assertNotNull(harvester.getSource("quality-dimension"));
        assertEquals("quality-dimension.ttl", harvester.getSource("quality-dimension").getFilename());
        assertEquals("2023-01-30", harvester.getVersion());

        List<QualityDimension> qualityDimensions = harvester.harvest().collectList().block();
        assertNotNull(qualityDimensions);
        assertEquals(QUALITY_DIMENSIONS_SIZE, qualityDimensions.size());

        QualityDimension first = qualityDimensions.get(0);
        assertEquals("https://data.norge.no/vocabulary/quality-dimension#currentness", first.getUri());
        assertEquals("currentness", first.getCode());
        assertEquals("currentness", first.getLabel().get(Language.ENGLISH.code()));
    }

}
