package no.fdk.referencedata.digdir.roletype;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class RoleTypeHarvesterTest {

    @Test
    public void test_fetch_role_types() {
        RoleTypeHarvester harvester = new LocalRoleTypeHarvester("123-0");

        assertNotNull(harvester.getSource("role-type"));
        assertEquals("role-type.ttl", harvester.getSource("role-type").getFilename());
        assertEquals("123-0", harvester.getVersion());

        List<RoleType> roleTypes = harvester.harvest().collectList().block();
        assertNotNull(roleTypes);
        assertEquals(5, roleTypes.size());

        RoleType first = roleTypes.get(0);
        assertEquals("https://data.norge.no/vocabulary/role-type#data-provider", first.getUri());
        assertEquals("data-provider", first.getCode());
        assertEquals("data provider", first.getLabel().get(Language.ENGLISH.code()));
    }

}
