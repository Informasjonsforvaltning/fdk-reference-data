package no.fdk.referencedata.digdir.evidencetype;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class EvidenceTypeHarvesterTest {

    @Test
    public void test_fetch_evidence_types() {
        EvidenceTypeHarvester harvester = new LocalEvidenceTypeHarvester("123-0");

        assertNotNull(harvester.getSource("evidence-type"));
        assertEquals("evidence-type.ttl", harvester.getSource("evidence-type").getFilename());
        assertEquals("123-0", harvester.getVersion());

        List<EvidenceType> evidenceTypes = harvester.harvest().collectList().block();
        assertNotNull(evidenceTypes);
        assertEquals(4, evidenceTypes.size());

        EvidenceType first = evidenceTypes.get(0);
        assertEquals("https://data.norge.no/vocabulary/evidence-type#protocol", first.getUri());
        assertEquals("protocol", first.getCode());
        assertEquals("protocol", first.getLabel().get(Language.ENGLISH.code()));
    }

}
