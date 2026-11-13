package no.fdk.referencedata.digdir.highvaluedatasettype;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class HighValueDatasetTypeHarvesterTest {

    @Test
    public void test_fetch_high_value_dataset_types() {
        HighValueDatasetTypeHarvester harvester = LocalHarvesters.highValueDatasetType();

        assertNotNull(harvester.getSource("high-value-dataset-type"));
        assertEquals("high-value-dataset-type.ttl", harvester.getSource("high-value-dataset-type").getFilename());

        List<HighValueDatasetType> highValueDatasetTypes = harvester.harvest().collectList().block();
        assertNotNull(highValueDatasetTypes);
        assertEquals(3, highValueDatasetTypes.size());

        HighValueDatasetType nationalHvd = highValueDatasetTypes.stream()
                .filter(t -> "national-hvd".equals(t.getCode()))
                .findFirst()
                .orElseThrow();
        assertEquals("https://data.norge.no/vocabulary/high-value-dataset-type#national-hvd", nationalHvd.getUri());
        assertEquals("National high-value dataset", nationalHvd.getLabel().get(Language.ENGLISH.code()));
        assertEquals("Nasjonalt datasett av høy verdi", nationalHvd.getLabel().get(Language.NORWEGIAN_BOKMAAL.code()));
    }

}
