package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.geonames.GeonamesFylke;
import no.fdk.referencedata.geonames.GeonamesFylkeRepository;
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
                "wiremock.host=dummy",
                "wiremock.port=0"
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class GeonamesFylkeQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GeonamesFylkeRepository geonamesFylkeRepository;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        geonamesFylkeRepository.deleteAll();
        geonamesFylkeRepository.saveAll(List.of(
                GeonamesFylke.builder()
                        .uri("https://sws.geonames.org/7626836/")
                        .geonameId("7626836")
                        .name("Agder")
                        .build(),
                GeonamesFylke.builder()
                        .uri("https://sws.geonames.org/3162656/")
                        .geonameId("3162656")
                        .name("Vestland")
                        .build()
        ));
    }

    @Test
    void test_if_geonames_fylker_query_returns_all_fylker() {
        List<GeonamesFylke> result = graphQlTester.documentName("geonames-fylker")
                .execute()
                .path("$['data']['geonamesFylker']")
                .entityList(GeonamesFylke.class)
                .get();

        assertEquals(2, result.size());

        GeonamesFylke first = result.get(0);
        assertEquals("7626836", first.getGeonameId());
        assertEquals("Agder", first.getName());
    }

    @Test
    void test_if_geonames_fylke_by_geoname_id_query_returns_correct_fylke() {
        GeonamesFylke result = graphQlTester.documentName("geonames-fylke-by-geoname-id")
                .variable("geonameId", "3162656")
                .execute()
                .path("$['data']['geonamesFylkeByGeonameId']")
                .entity(GeonamesFylke.class)
                .get();

        assertEquals("3162656", result.getGeonameId());
        assertEquals("Vestland", result.getName());
    }

    @Test
    void test_if_geonames_fylke_by_geoname_id_unknown_returns_null() {
        graphQlTester.documentName("geonames-fylke-by-geoname-id")
                .variable("geonameId", "unknown")
                .execute()
                .path("$['data']['geonamesFylkeByGeonameId']")
                .valueIsNull();
    }
}
