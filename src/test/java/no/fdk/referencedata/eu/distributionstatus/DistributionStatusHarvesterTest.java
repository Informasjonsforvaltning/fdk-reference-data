package no.fdk.referencedata.eu.distributionstatus;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static no.fdk.referencedata.eu.distributionstatus.LocalDistributionStatusHarvester.DISTRIBUTION_STATUS_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class DistributionStatusHarvesterTest {

    @Test
    public void test_fetch_distribution_statuses() {
        DistributionStatusHarvester harvester = new LocalDistributionStatusHarvester("20220615-0");

        assertNotNull(harvester.getSource());
        assertEquals("distribution-status-sparql-result.ttl", harvester.getSource().getFilename());
        assertEquals("20220615-0", harvester.getVersion());

        List<DistributionStatus> distributionStatuses = harvester.harvest().collectList().block();
        assertNotNull(distributionStatuses);
        assertEquals(DISTRIBUTION_STATUS_SIZE, distributionStatuses.size());

        DistributionStatus first = distributionStatuses.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/distribution-status/DEPRECATED", first.getUri());
        assertEquals("DEPRECATED", first.getCode());
        assertEquals("deprecated", first.getLabel().get(Language.ENGLISH.code()));
    }

}
