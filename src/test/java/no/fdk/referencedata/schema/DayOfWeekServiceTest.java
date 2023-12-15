package no.fdk.referencedata.schema;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.schema.dayofweek.DayOfWeek;
import no.fdk.referencedata.schema.dayofweek.DayOfWeekImporter;
import no.fdk.referencedata.schema.dayofweek.DayOfWeekService;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ActiveProfiles("test")
public class DayOfWeekServiceTest {

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_get_all_returns_all_week_days() {
        DayOfWeekService service = new DayOfWeekService(new DayOfWeekImporter(), rdfSourceRepository);
        service.importWeekDays();

        List<DayOfWeek> weekDays = service.getAll();
        assertEquals(8, weekDays.size());

        DayOfWeek first = weekDays.get(0);
        assertEquals("https://schema.org/Friday", first.getUri());
        assertEquals("Friday", first.getCode());
        assertEquals("Friday", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_by_code_returns_correct_day() {
        DayOfWeekService service = new DayOfWeekService(new DayOfWeekImporter(), rdfSourceRepository);
        service.importWeekDays();

        Optional<DayOfWeek> dayOfWeekOptional = service.getByCode("PublicHolidays");

        assertTrue(dayOfWeekOptional.isPresent());

        DayOfWeek dayOfWeek = dayOfWeekOptional.get();
        assertEquals("https://schema.org/PublicHolidays", dayOfWeek.getUri());
        assertEquals("PublicHolidays", dayOfWeek.getCode());
        assertEquals("Public holidays", dayOfWeek.getLabel().get(Language.ENGLISH.code()));
    }

}
