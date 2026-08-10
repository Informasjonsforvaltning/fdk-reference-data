package no.fdk.referencedata.graphql;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.eu.continent.Continent;
import no.fdk.referencedata.container.AbstractContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
                "wiremock.host=dummy",
                "wiremock.port=0"
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class ContinentQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        HarvestTestSupport.harvest(registry, "continent");
    }

    @Test
    void test_if_continents_query_returns_all_continents() {
        GraphQlTester.EntityList<Continent> result = graphQlTester.documentName("continents")
                .execute()
                .path("$['data']['continents']")
                .entityList(Continent.class);
        Continent continent = result.get().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/continent/AFRICA", continent.getUri());
        assertEquals("AFRICA", continent.getCode());
        assertEquals("Africa", continent.getLabel().get("en"));
    }

    @Test
    void test_if_continent_by_code_query_returns_correct_continent() {
        Continent result = graphQlTester.documentName("continent-by-code")
                .variable("code", "EUROPE")
                .execute()
                .path("$['data']['continentByCode']")
                .entity(Continent.class)
                .get();

        assertEquals("http://publications.europa.eu/resource/authority/continent/EUROPE", result.getUri());
        assertEquals("EUROPE", result.getCode());
        assertEquals("Europe", result.getLabel().get("en"));
    }

}
