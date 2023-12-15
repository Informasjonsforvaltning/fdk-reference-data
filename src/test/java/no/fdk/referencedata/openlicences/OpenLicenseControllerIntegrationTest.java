package no.fdk.referencedata.openlicences;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "scheduling.enabled=false",
        })
@ActiveProfiles("test")
public class OpenLicenseControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test_if_get_all_open_licenses_returns_valid_response() {
        OpenLicenses licenses =
                this.restTemplate.getForObject("http://localhost:" + port + "/open-licenses", OpenLicenses.class);

        assertEquals(3, licenses.getOpenLicenses().size());

        OpenLicense license = licenses.getOpenLicenses().get(1);
        assertEquals("http://publications.europa.eu/resource/authority/licence/CC_BY_4_0", license.getUri());
        assertEquals("CC BY 4.0", license.getCode());
        assertEquals("Creative Commons Attribution 4.0 International", license.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_open_license_by_code_returns_valid_response() {
        OpenLicense license =
                this.restTemplate.getForObject("http://localhost:" + port + "/open-licenses/NLOD20", OpenLicense.class);

        assertNotNull(license);
        assertEquals("http://publications.europa.eu/resource/authority/licence/NLOD_2_0", license.getUri());
        assertEquals("NLOD20", license.getCode());
        assertEquals("Norwegian Licence for Open Government Data", license.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_open_licenses_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/open-licenses", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(OpenLicenseControllerIntegrationTest.class.getClassLoader().getResource("rdf/open-licenses.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
