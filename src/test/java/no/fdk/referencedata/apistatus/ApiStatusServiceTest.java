package no.fdk.referencedata.apistatus;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
public class ApiStatusServiceTest {

    @Test
    public void test_if_get_all_returns_all_api_statuses() {
        ApiStatusService service = new ApiStatusService(new ApiStatusImporter());
        service.importApiStatuses();

        List<ApiStatus> statuses = service.getAll();
        assertEquals(4, statuses.size());

        ApiStatus first = statuses.get(0);
        assertEquals("http://fellesdatakatalog.brreg.no/reference-data/codes/apistastus/deprecated", first.getUri());
        assertEquals("REMOVED", first.getCode());
        assertEquals("Removed", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_api_status_by_code_returns_correct_api_status() {
        ApiStatusService service = new ApiStatusService(new ApiStatusImporter());
        service.importApiStatuses();

        Optional<ApiStatus> statusOptional = service.getByCode("EXPERIMENTAL");

        assertTrue(statusOptional.isPresent());

        ApiStatus apiStatus = statusOptional.get();
        assertEquals("http://fellesdatakatalog.brreg.no/reference-data/codes/apistastus/nonproduction", apiStatus.getUri());
        assertEquals("EXPERIMENTAL", apiStatus.getCode());
        assertEquals("Experimental", apiStatus.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_api_status_model_is_isomorphic_with_old_rdf() {
        Model expected = ModelFactory.createDefaultModel();
        expected.read(requireNonNull(ApiStatusServiceTest.class.getClassLoader().getResource("api-status-skos.ttl")).toString());

        ApiStatusService service = new ApiStatusService(new ApiStatusImporter());
        service.importApiStatuses();

        Model result = ModelFactory.createDefaultModel();
        result.read(new StringReader(service.getRdf(RDFFormat.TURTLE)), "http://base.example.com", "TURTLE");

        assertTrue(result.isIsomorphicWith(expected));
    }

}
