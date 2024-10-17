package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.geonorge.administrativeenheter.nasjon.Nasjon;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class NasjonQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void test_if_nasjon_query_returns_all_nasjoner() {
        List<Nasjon> result = graphQlTester.documentName("nasjoner")
                .execute()
                .path("$['data']['nasjoner']")
                .entityList(Nasjon.class)
                .get();

        assertEquals(1, result.size());

        Nasjon nasjon = result.get(0);

        assertEquals("https://data.geonorge.no/administrativeEnheter/nasjon/id/173163", nasjon.getUri());
        assertEquals("Norge", nasjon.getNasjonsnavn());
        assertEquals("173163", nasjon.getNasjonsnummer());
    }

    @Test
    void test_if_nasjon_by_nasjonsnummer_query_returns_norge() {
        Nasjon result = graphQlTester.documentName("nasjon-by-nasjonsnummer")
                .variable("nasjonsnummer", "173163")
                .execute()
                .path("$['data']['nasjonByNasjonsnummer']")
                .entity(Nasjon.class)
                .get();

        assertEquals("https://data.geonorge.no/administrativeEnheter/nasjon/id/173163", result.getUri());
    }

    @Test
    void test_if_nasjon_by_nasjonsnummer_query_returns_null() {
        graphQlTester.documentName("nasjon-by-nasjonsnummer")
                .variable("nasjonsnummer", "11111")
                .execute()
                .path("$['data']['nasjonByNasjonsnummer']")
                .valueIsNull();
    }
}
