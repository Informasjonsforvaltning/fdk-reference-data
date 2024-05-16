package no.fdk.referencedata.digdir.audiencetype;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class AudienceTypeHarvesterTest {

    @Test
    public void test_fetch_audience_types() {
        AudienceTypeHarvester harvester = new LocalAudienceTypeHarvester("123-0");

        assertNotNull(harvester.getSource("audience-type"));
        assertEquals("audience-type.ttl", harvester.getSource("audience-type").getFilename());
        assertEquals("123-0", harvester.getVersion());

        List<AudienceType> audienceTypes = harvester.harvest().collectList().block();
        assertNotNull(audienceTypes);
        assertEquals(2, audienceTypes.size());

        AudienceType first = audienceTypes.get(0);
        assertEquals("https://data.norge.no/vocabulary/audience-type#public", first.getUri());
        assertEquals("public", first.getCode());
        assertEquals("public", first.getLabel().get(Language.ENGLISH.code()));
    }

}
