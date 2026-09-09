package no.fdk.referencedata.eu.distributionstatus;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static no.fdk.referencedata.LocalHarvestFixtures.DISTRIBUTION_STATUS_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class DistributionStatusHarvesterTest {

    @Test
    public void test_fetch_distribution_statuses() {
        DistributionStatusHarvester harvester = LocalHarvesters.distributionStatus();

        assertNotNull(harvester.getSource());
        assertEquals("distribution-status-sparql-result.ttl", harvester.getSource().getFilename());

        List<DistributionStatus> distributionStatuses = harvester.harvest().collectList().block();
        assertNotNull(distributionStatuses);
        assertEquals(DISTRIBUTION_STATUS_SIZE, distributionStatuses.size());

        DistributionStatus first = distributionStatuses.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/distribution-status/DEPRECATED", first.getUri());
        assertEquals("DEPRECATED", first.getCode());
        assertEquals("deprecated", first.getLabel().get(Language.ENGLISH.code()));

        DistributionStatus ongoing = distributionStatuses.stream()
                .filter(status -> "ONGOING".equals(status.getCode()))
                .findFirst()
                .orElseThrow();
        assertEquals("ongoing", ongoing.getLabel().get(Language.ENGLISH.code()));
        assertEquals("pågående", ongoing.getLabel().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertEquals("pågåande", ongoing.getLabel().get(Language.NORWEGIAN_NYNORSK.code()));

        DistributionStatus required = distributionStatuses.stream()
                .filter(status -> "REQUIRED".equals(status.getCode()))
                .findFirst()
                .orElseThrow();
        assertEquals("required", required.getLabel().get(Language.ENGLISH.code()));
        assertEquals("påkrevd", required.getLabel().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertEquals("påkravd", required.getLabel().get(Language.NORWEGIAN_NYNORSK.code()));

        DistributionStatus archive = distributionStatuses.stream()
                .filter(status -> "ARCHIVE".equals(status.getCode()))
                .findFirst()
                .orElseThrow();
        assertEquals("historical archive", archive.getLabel().get(Language.ENGLISH.code()));
        assertEquals("arkivert", archive.getLabel().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertEquals("arkivert", archive.getLabel().get(Language.NORWEGIAN_NYNORSK.code()));

        DistributionStatus planned = distributionStatuses.stream()
                .filter(status -> "PLANNED".equals(status.getCode()))
                .findFirst()
                .orElseThrow();
        assertEquals("planned", planned.getLabel().get(Language.ENGLISH.code()));
        assertEquals("planlagt", planned.getLabel().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertEquals("planlagt", planned.getLabel().get(Language.NORWEGIAN_NYNORSK.code()));
    }

}
