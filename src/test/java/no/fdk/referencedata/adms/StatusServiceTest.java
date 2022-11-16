package no.fdk.referencedata.adms;

import no.fdk.referencedata.adms.status.ADMSStatus;
import no.fdk.referencedata.adms.status.ADMSStatusImporter;
import no.fdk.referencedata.adms.status.ADMSStatusService;
import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
public class StatusServiceTest {

    @Test
    public void test_if_get_all_returns_all_statuses() {
        ADMSStatusService service = new ADMSStatusService(new ADMSStatusImporter());
        service.importADMSStatuses();

        List<ADMSStatus> statuses = service.getAll();
        assertEquals(4, statuses.size());

        ADMSStatus first = statuses.get(0);
        assertEquals("http://purl.org/adms/status/Completed", first.getUri());
        assertEquals("Completed", first.getCode());
        assertEquals("Completed", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_status_by_code_returns_correct_status() {
        ADMSStatusService service = new ADMSStatusService(new ADMSStatusImporter());
        service.importADMSStatuses();

        Optional<ADMSStatus> statusOptional = service.getByCode("Deprecated");

        assertTrue(statusOptional.isPresent());

        ADMSStatus status = statusOptional.get();
        assertEquals("http://purl.org/adms/status/Deprecated", status.getUri());
        assertEquals("Deprecated", status.getCode());
        assertEquals("Deprecated", status.getLabel().get(Language.ENGLISH.code()));
    }

}
