package no.fdk.referencedata.openlicences;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
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
import static org.mockito.Mockito.mock;

@ActiveProfiles("test")
public class OpenLicenseServiceTest {

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_get_all_returns_all_open_licenses() {
        OpenLicenseService service = new OpenLicenseService(new OpenLicenseImporter(), rdfSourceRepository);
        service.importOpenLicenses();

        List<OpenLicense> licenses = service.getAll();
        assertEquals(3, licenses.size());

        OpenLicense first = licenses.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/licence/CC0", first.getUri());
        assertEquals("CC0 1.0", first.getCode());
        assertEquals("Creative Commons Universal Public Domain Dedication", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_license_by_code_returns_correct_api_specification() {
        OpenLicenseService service = new OpenLicenseService(new OpenLicenseImporter(), rdfSourceRepository);
        service.importOpenLicenses();

        Optional<OpenLicense> licenseOptional = service.getByCode("NLOD20");

        assertTrue(licenseOptional.isPresent());

        OpenLicense license = licenseOptional.get();
        assertEquals("http://publications.europa.eu/resource/authority/licence/NLOD_2_0", license.getUri());
        assertEquals("NLOD20", license.getCode());
        assertEquals("Norsk lisens for offentlige data", license.getLabel().get(Language.NORWEGIAN.code()));
    }

}
