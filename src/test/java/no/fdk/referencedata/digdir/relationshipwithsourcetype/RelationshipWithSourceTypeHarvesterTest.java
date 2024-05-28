package no.fdk.referencedata.digdir.relationshipwithsourcetype;


import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class RelationshipWithSourceTypeHarvesterTest {

    @Test
    public void test_fetch_relationship_with_source_types() {
        RelationshipWithSourceTypeHarvester harvester = new LocalRelationshipWithSourceTypeHarvester("123-0");

        assertNotNull(harvester.getSource("relationship-with-source-type"));
        assertEquals("relationship-with-source-type.ttl", harvester.getSource("relationship-with-source-type").getFilename());
        assertEquals("123-0", harvester.getVersion());

        List<RelationshipWithSourceType> relationshipWithSourceTypes = harvester.harvest().collectList().block();
        assertNotNull(relationshipWithSourceTypes);
        assertEquals(3, relationshipWithSourceTypes.size());

        RelationshipWithSourceType first = relationshipWithSourceTypes.stream().sorted(Comparator.comparing(h -> h.code)).findFirst().get();
        assertEquals("https://data.norge.no/vocabulary/relationship-with-source-type#derived-from-source", first.getUri());
        assertEquals("derived-from-source", first.getCode());
        assertEquals("derived from source", first.getLabel().get(Language.ENGLISH.code()));
    }

}
