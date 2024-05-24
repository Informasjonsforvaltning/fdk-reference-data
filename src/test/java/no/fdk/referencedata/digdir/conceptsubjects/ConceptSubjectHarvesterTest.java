package no.fdk.referencedata.digdir.conceptsubjects;

import no.fdk.referencedata.ApplicationSettings;
import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class ConceptSubjectHarvesterTest {

    @Test
    public void test_fetch_concept_subjects() {
        ConceptSubjectHarvester harvester = new LocalConceptSubjectHarvester(new ApplicationSettings());

        assertNotNull(harvester.getSource());
        assertEquals("concept-subjects.ttl", harvester.getSource().getFilename());
        assertEquals("0", harvester.getVersion());

        List<ConceptSubject> subjects = harvester.harvest().collectList().block();
        assertNotNull(subjects);
        assertEquals(4, subjects.size());

        ConceptSubject first = subjects.get(0);
        assertEquals("https://catalog-admin-service.staging.fellesdatakatalog.digdir.no/910244132/concepts/subjects#555", first.getUri());
        assertEquals("555", first.getCode());
        assertEquals(null, first.getLabel().get(Language.NORWEGIAN_BOKMAAL.code()));
    }

}
