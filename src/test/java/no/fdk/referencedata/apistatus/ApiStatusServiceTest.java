package no.fdk.referencedata.apistatus;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ActiveProfiles("test")
public class ApiStatusServiceTest {

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_get_all_returns_all_api_statuses() {
        ApiStatusService service = new ApiStatusService(new ApiStatusImporter(), rdfSourceRepository);
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
        ApiStatusService service = new ApiStatusService(new ApiStatusImporter(), rdfSourceRepository);
        service.importApiStatuses();

        Optional<ApiStatus> statusOptional = service.getByCode("EXPERIMENTAL");

        assertTrue(statusOptional.isPresent());

        ApiStatus apiStatus = statusOptional.get();
        assertEquals("http://fellesdatakatalog.brreg.no/reference-data/codes/apistastus/nonproduction", apiStatus.getUri());
        assertEquals("EXPERIMENTAL", apiStatus.getCode());
        assertEquals("Experimental", apiStatus.getLabel().get(Language.ENGLISH.code()));
    }

}
