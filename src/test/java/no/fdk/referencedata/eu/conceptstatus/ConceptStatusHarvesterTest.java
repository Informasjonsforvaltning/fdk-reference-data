package no.fdk.referencedata.eu.conceptstatus;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.Comparator;
import java.util.List;

import static no.fdk.referencedata.eu.conceptstatus.LocalConceptStatusHarvester.CONCEPT_STATUSES_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class ConceptStatusHarvesterTest {

    @Test
    public void test_fetch_concept_statuses() {
        ConceptStatusHarvester harvester = new LocalConceptStatusHarvester("20200923-0");

        assertNotNull(harvester.getSource());
        assertEquals("concept-status.ttl", harvester.getSource().getFilename());
        assertEquals("20200923-0", harvester.getVersion());

        List<ConceptStatus> statuses = harvester.harvest().collectList().block();
        assertNotNull(statuses);
        assertEquals(CONCEPT_STATUSES_SIZE, statuses.size());

        statuses.sort(Comparator.comparing(ConceptStatus::getUri));
        ConceptStatus first = statuses.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/concept-status/CANDIDATE", first.getUri());
        assertEquals("CANDIDATE", first.getCode());
        assertEquals("candidate", first.getLabel().get(Language.ENGLISH.code()));
    }

}
