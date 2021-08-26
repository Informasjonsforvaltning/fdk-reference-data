package no.fdk.referencedata.eu.accessright;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.mongo.AbstractMongoDbContainerTest;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
public class AccessRightControllerIntegrationTest extends AbstractMongoDbContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AccessRightRepository accessRightRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        AccessRightService accessRightService = new AccessRightService(
                new LocalAccessRightHarvester("1"),
                accessRightRepository,
                harvestSettingsRepository);

        accessRightService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_access_rights_returns_valid_response() {
        AccessRights accessRights =
                this.restTemplate.getForObject("http://localhost:" + port + "/eu/access-rights", AccessRights.class);

        assertEquals(6, accessRights.getAccessRights().size());

        AccessRight first = accessRights.getAccessRights().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/access-right/CONFIDENTIAL", first.getUri());
        assertEquals("CONFIDENTIAL", first.getCode());
        assertEquals("confidential", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_access_right_by_code_returns_valid_response() {
        AccessRight accessRight =
                this.restTemplate.getForObject("http://localhost:" + port + "/eu/access-rights/CONFIDENTIAL", AccessRight.class);

        assertNotNull(accessRight);
        assertEquals("http://publications.europa.eu/resource/authority/access-right/CONFIDENTIAL", accessRight.getUri());
        assertEquals("CONFIDENTIAL", accessRight.getCode());
        assertEquals("confidential", accessRight.getLabel().get(Language.ENGLISH.code()));
    }
}
