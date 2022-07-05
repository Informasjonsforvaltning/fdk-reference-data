package no.fdk.referencedata.openlicences;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "scheduling.enabled=false",
        })
@ActiveProfiles("test")
public class OpenLicenseControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test_if_get_all_open_licenses_returns_valid_response() {
        OpenLicenses licenses =
                this.restTemplate.getForObject("http://localhost:" + port + "/open-licenses", OpenLicenses.class);

        assertEquals(7, licenses.getOpenLicenses().size());

        OpenLicense first = licenses.getOpenLicenses().get(0);
        assertEquals("http://creativecommons.org/licenses/by/4.0/", first.getUri());
        assertEquals("CC BY 4.0", first.getCode());
        assertNull(first.getIsReplacedBy());
        assertEquals("Creative Commons Attribution 4.0 International", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_open_license_by_code_returns_valid_response() {
        OpenLicense license =
                this.restTemplate.getForObject("http://localhost:" + port + "/open-licenses/NLOD20", OpenLicense.class);

        assertNotNull(license);
        assertEquals("http://data.norge.no/nlod/no/2.0", license.getUri());
        assertEquals("NLOD20", license.getCode());
        assertNull(license.getIsReplacedBy());
        assertEquals("Norwegian Licence for Open Government Data", license.getLabel().get(Language.ENGLISH.code()));
    }
}