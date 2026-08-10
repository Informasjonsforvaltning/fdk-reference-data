package no.fdk.referencedata.graphql;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.eu.frequency.Frequency;
import no.fdk.referencedata.container.AbstractContainerTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class FrequencyQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        HarvestTestSupport.harvest(registry, "frequency");
    }

    @Test
    void test_if_frequencies_query_returns_all_frequencies() {
            List<Frequency> result = graphQlTester.documentName("frequencies")
                    .execute()
                    .path("$['data']['frequencies']")
                    .entityList(Frequency.class)
                    .get();
            Frequency frequency = result.get(0);

            assertEquals("http://publications.europa.eu/resource/authority/frequency/10MIN", frequency.getUri());
            assertEquals("10MIN", frequency.getCode());
            assertEquals("hvert tiende minutt", frequency.getLabel().get("nb"));
            assertEquals("kvart tiande minutt", frequency.getLabel().get("nn"));
            assertEquals("every ten minutes", frequency.getLabel().get("en"));
    }

    @Test
    void test_if_frequency_by_code_public_query_returns_public_frequency() {
            Frequency result = graphQlTester.documentName("frequency-by-code")
                    .variable("code", "WEEKLY_3")
                    .execute()
                    .path("$['data']['frequencyByCode']")
                    .entity(Frequency.class)
                    .get();

        assertEquals("http://publications.europa.eu/resource/authority/frequency/WEEKLY_3", result.getUri());
        assertEquals("WEEKLY_3", result.getCode());
        assertEquals("tre ganger i uken", result.getLabel().get("no"));
            assertEquals("tre ganger i uken", result.getLabel().get("nb"));
            assertEquals("tre gongar i veka", result.getLabel().get("nn"));
            assertEquals("three times a week", result.getLabel().get("en"));
    }

}
