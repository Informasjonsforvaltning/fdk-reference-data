package no.fdk.referencedata.digdir.legalresourcetype;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static no.fdk.referencedata.digdir.legalresourcetype.LocalLegalResourceTypeHarvester.LEGAL_RESOURCE_TYPES_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class LegalResourceTypeHarvesterTest {

    @Test
    public void test_fetch_legal_resource_types() {
        LegalResourceTypeHarvester harvester = new LocalLegalResourceTypeHarvester("2023-08-17");

        assertNotNull(harvester.getSource("legal-resource-type"));
        assertEquals("legal-resource-type.ttl", harvester.getSource("legal-resource-type").getFilename());
        assertEquals("2023-08-17", harvester.getVersion());

        List<LegalResourceType> legalResourceTypes = harvester.harvest().collectList().block();
        assertNotNull(legalResourceTypes);
        assertEquals(LEGAL_RESOURCE_TYPES_SIZE, legalResourceTypes.size());

        LegalResourceType first = legalResourceTypes.get(0);
        assertEquals("https://data.norge.no/vocabulary/legal-resource-type#regulation", first.getUri());
        assertEquals("regulation", first.getCode());
        assertEquals("regulation", first.getLabel().get(Language.ENGLISH.code()));
    }

}
