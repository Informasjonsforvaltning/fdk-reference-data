package no.fdk.referencedata.eu.distributiontype;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class DistributionTypeHarvesterTest {

    @Test
    public void test_fetch_distribution_types() {
        DistributionTypeHarvester harvester = new LocalDistributionTypeHarvester("20200923-0");

        assertNotNull(harvester.getSource());
        assertEquals("distribution-types-sparql-result.ttl", harvester.getSource().getFilename());
        assertEquals("20200923-0", harvester.getVersion());

        List<DistributionType> distributionTypes = harvester.harvest().collectList().block();
        assertNotNull(distributionTypes);
        assertEquals(5, distributionTypes.size());

        DistributionType first = distributionTypes.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/distribution-type/FEED_INFO", first.getUri());
        assertEquals("FEED_INFO", first.getCode());
        assertEquals("Information feed", first.getLabel().get(Language.ENGLISH.code()));
    }

}
