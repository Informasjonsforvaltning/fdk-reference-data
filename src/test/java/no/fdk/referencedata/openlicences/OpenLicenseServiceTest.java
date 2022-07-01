package no.fdk.referencedata.openlicences;

import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFFormat;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
public class OpenLicenseServiceTest {

    @Test
    public void test_if_get_all_returns_all_open_licenses() {
        OpenLicenseService service = new OpenLicenseService(new OpenLicenseImporter());
        service.importOpenLicenses();

        List<OpenLicense> licenses = service.getAll();
        assertEquals(7, licenses.size());

        OpenLicense first = licenses.get(0);
        assertEquals("http://creativecommons.org/licenses/by/4.0/", first.getUri());
        assertEquals("CC BY 4.0", first.getCode());
        assertNull(first.getIsReplacedBy());
        assertEquals("Creative Commons Attribution 4.0 International", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_license_by_code_returns_correct_api_specification() {
        OpenLicenseService service = new OpenLicenseService(new OpenLicenseImporter());
        service.importOpenLicenses();

        Optional<OpenLicense> licenseOptional = service.getByCode("NLOD10");

        assertTrue(licenseOptional.isPresent());

        OpenLicense license = licenseOptional.get();
        assertEquals("http://data.norge.no/nlod/no/1.0", license.getUri());
        assertEquals("NLOD10", license.getCode());
        assertEquals("http://data.norge.no/nlod/no/2.0", license.getIsReplacedBy());
        assertEquals("Norwegian Licence for Open Government Data", license.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_open_license_model_is_isomorphic_with_old_rdf() {
        Model expected = ModelFactory.createDefaultModel();
        expected.read(requireNonNull(OpenLicenseServiceTest.class.getClassLoader().getResource("open-licenses-skos.rdf")).toString());

        OpenLicenseService service = new OpenLicenseService(new OpenLicenseImporter());
        service.importOpenLicenses();

        Model result = ModelFactory.createDefaultModel();
        result.read(new StringReader(service.getRdf(RDFFormat.TURTLE)), "http://base.example.com", "TURTLE");

        assertTrue(result.isIsomorphicWith(expected));
    }

}
