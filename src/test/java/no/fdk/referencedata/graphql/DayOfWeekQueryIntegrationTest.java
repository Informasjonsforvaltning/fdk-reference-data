package no.fdk.referencedata.graphql;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.schema.dayofweek.DayOfWeek;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@ActiveProfiles("test")
class DayOfWeekQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void test_if_week_days_query_returns_valid_response() {
        List<DayOfWeek> result = graphQlTester.documentName("week-days")
                .execute()
                .path("$['data']['weekDays']")
                .entityList(DayOfWeek.class)
                .get();

        Assertions.assertEquals(8, result.size());

        DayOfWeek day = result.get(1);

        assertEquals("https://schema.org/Monday", day.getUri());
        assertEquals("Monday", day.getCode());
        assertEquals("Mandag", day.getLabel().get("nb"));
        assertEquals("Måndag", day.getLabel().get("nn"));
        assertEquals("Monday", day.getLabel().get("en"));
    }

    @Test
    void test_if_day_of_week_by_code_query_returns_valid_response() {
        DayOfWeek result = graphQlTester.documentName("day-of-week-by-code")
                .variable("code", "Tuesday")
                .execute()
                .path("$['data']['dayOfWeekByCode']")
                .entity(DayOfWeek.class)
                .get();

        assertEquals("https://schema.org/Tuesday", result.getUri());
        assertEquals("Tuesday", result.getCode());
        assertEquals("Tirsdag", result.getLabel().get("nb"));
        assertEquals("Tysdag", result.getLabel().get("nn"));
        assertEquals("Tuesday", result.getLabel().get("en"));
    }

    @Test
    void test_if_invalid_day_of_week_by_code_query_returns_null() {
        graphQlTester.documentName("day-of-week-by-code")
                .variable("code", "INVALID")
                .execute()
                .path("$['data']['dayOfWeekByCode']")
                .valueIsNull();
    }
}
