package no.fdk.referencedata.eu.accessright;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.Comparator;
import java.util.List;

import static no.fdk.referencedata.LocalHarvestFixtures.ACCESS_RIGHTS_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class AccessRightHarvesterTest {

    @Test
    public void test_fetch_access_rights() {
        AccessRightHarvester harvester = LocalHarvesters.accessRight();

        assertNotNull(harvester.getSource());
        assertEquals("access-right-sparql-result.ttl", harvester.getSource().getFilename());

        List<AccessRight> accessRights = harvester.harvest().collectList().block();
        assertNotNull(accessRights);
        assertEquals(ACCESS_RIGHTS_SIZE, accessRights.size());

        accessRights.sort(Comparator.comparing(AccessRight::getUri));
        AccessRight first = accessRights.get(2);
        assertEquals("http://publications.europa.eu/resource/authority/access-right/NORMAL", first.getUri());
        assertEquals("NORMAL", first.getCode());
        assertEquals("normal", first.getLabel().get(Language.ENGLISH.code()));
    }

}
