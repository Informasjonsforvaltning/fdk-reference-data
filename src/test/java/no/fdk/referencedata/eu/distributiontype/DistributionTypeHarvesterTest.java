package no.fdk.referencedata.eu.distributiontype;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static no.fdk.referencedata.LocalHarvestFixtures.DISTRIBUTION_TYPES_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class DistributionTypeHarvesterTest {

    @Test
    public void test_fetch_distribution_types() {
        DistributionTypeHarvester harvester = LocalHarvesters.distributionType();

        assertNotNull(harvester.getSource());
        assertEquals("distribution-types-sparql-result.ttl", harvester.getSource().getFilename());

        List<DistributionType> distributionTypes = harvester.harvest().collectList().block();
        assertNotNull(distributionTypes);
        assertEquals(DISTRIBUTION_TYPES_SIZE, distributionTypes.size());

        DistributionType first = distributionTypes.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/distribution-type/FEED_INFO", first.getUri());
        assertEquals("FEED_INFO", first.getCode());
        assertEquals("Information feed", first.getLabel().get(Language.ENGLISH.code()));
    }

}
