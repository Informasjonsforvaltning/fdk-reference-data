package no.fdk.referencedata.schema;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.schema.dayofweek.DayOfWeek;
import no.fdk.referencedata.schema.dayofweek.WeekDays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "scheduling.enabled=false",
        })
@ActiveProfiles("test")
public class DayOfWeekControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test_if_get_week_days_returns_valid_response() {
        WeekDays weekDays =
                this.restTemplate.getForObject("http://localhost:" + port + "/schema/week-days", WeekDays.class);

        assertEquals(8, weekDays.getWeekDays().size());

        DayOfWeek dayOfWeek = weekDays.getWeekDays().get(1);
        assertEquals("https://schema.org/Monday", dayOfWeek.getUri());
        assertEquals("Monday", dayOfWeek.getCode());
        assertEquals("Monday", dayOfWeek.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_day_of_week_returns_valid_response() {
        DayOfWeek dayOfWeek =
                this.restTemplate.getForObject("http://localhost:" + port + "/schema/week-days/Saturday", DayOfWeek.class);

        assertNotNull(dayOfWeek);
        assertEquals("https://schema.org/Saturday", dayOfWeek.getUri());
        assertEquals("Saturday", dayOfWeek.getCode());
        assertEquals("Saturday", dayOfWeek.getLabel().get(Language.ENGLISH.code()));
    }
}
