package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import no.fdk.referencedata.container.AbstractContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@ActiveProfiles("test")
class DayOfWeekQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private GraphQLTestTemplate template;

    @Test
    void test_if_week_days_query_returns_valid_response() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/week-days.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://schema.org/Monday", response.get("$['data']['weekDays'][1]['uri']"));
        assertEquals("Monday", response.get("$['data']['weekDays'][1]['code']"));
        assertEquals("Monday", response.get("$['data']['weekDays'][1]['label']['en']"));
    }

    @Test
    void test_if_day_of_week_by_code_query_returns_valid_response() throws IOException {
        GraphQLResponse response = template.perform("graphql/day-of-week-by-code.graphql",
                mapper.valueToTree(Map.of("code", "Tuesday")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://schema.org/Tuesday", response.get("$['data']['dayOfWeekByCode']['uri']"));
        assertEquals("Tuesday", response.get("$['data']['dayOfWeekByCode']['code']"));
        assertEquals("Tuesday", response.get("$['data']['dayOfWeekByCode']['label']['en']"));
    }

    @Test
    void test_if_invalid_day_of_week_by_code_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/day-of-week-by-code.graphql",
                mapper.valueToTree(Map.of("code", "INVALID")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['dayOfWeekByCode']"));
    }
}
