package no.fdk.referencedata.eu.accessright;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AccessRightHarvesterTest {

    @Test
    public void test_fetch_access_rights() {
        AccessRightHarvester harvester = new LocalAccessRightHarvester("20200923-0");

        assertNotNull(harvester.getSource());
        assertEquals("access-right-skos-ap-act.rdf", harvester.getSource().getFilename());
        assertEquals("20200923-0", harvester.getVersion());

        List<AccessRight> accessRights = harvester.harvest().collectList().block();
        assertNotNull(accessRights);
        assertEquals(6, accessRights.size());

        AccessRight first = accessRights.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/access-right/CONFIDENTIAL", first.getUri());
        assertEquals("CONFIDENTIAL", first.getCode());
        assertEquals("confidential", first.getLabel().get(Language.ENGLISH.code()));
    }

}
