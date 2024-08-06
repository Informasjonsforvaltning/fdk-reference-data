package no.fdk.referencedata.eu.datasettype;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static no.fdk.referencedata.eu.datasettype.LocalDatasetTypeHarvester.DATASET_TYPES_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class DatasetTypeHarvesterTest {

    @Test
    public void test_fetch_dataset_types() {
        DatasetTypeHarvester harvester = new LocalDatasetTypeHarvester("20200923-0");

        assertNotNull(harvester.getSource());
        assertEquals("dataset-types-sparql-result.ttl", harvester.getSource().getFilename());
        assertEquals("20200923-0", harvester.getVersion());

        List<DatasetType> datasetTypes = harvester.harvest().collectList().block();
        assertNotNull(datasetTypes);
        assertEquals(DATASET_TYPES_SIZE, datasetTypes.size());

        DatasetType first = datasetTypes.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/dataset-type/DSCRP_SERV", first.getUri());
        assertEquals("DSCRP_SERV", first.getCode());
        assertEquals("Service description", first.getLabel().get(Language.ENGLISH.code()));
    }

}
