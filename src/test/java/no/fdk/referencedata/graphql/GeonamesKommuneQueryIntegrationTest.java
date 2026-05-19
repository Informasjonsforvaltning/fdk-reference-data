package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.geonames.GeonamesKommune;
import no.fdk.referencedata.geonames.GeonamesKommuneRepository;
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
class GeonamesKommuneQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GeonamesKommuneRepository geonamesKommuneRepository;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        geonamesKommuneRepository.deleteAll();
        geonamesKommuneRepository.saveAll(List.of(
                GeonamesKommune.builder()
                        .uri("https://sws.geonames.org/3162994/")
                        .geonameId("3162994")
                        .name("Bergen")
                        .fylkeGeonameId("3162656")
                        .build(),
                GeonamesKommune.builder()
                        .uri("https://sws.geonames.org/3141558/")
                        .geonameId("3141558")
                        .name("Stavanger")
                        .fylkeGeonameId("607872")
                        .build()
        ));
    }

    @Test
    void test_if_geonames_kommuner_query_returns_all_kommuner() {
        List<GeonamesKommune> result = graphQlTester.documentName("geonames-kommuner")
                .execute()
                .path("$['data']['geonamesKommuner']")
                .entityList(GeonamesKommune.class)
                .get();

        assertEquals(2, result.size());

        GeonamesKommune first = result.get(0);
        assertEquals("3162994", first.getGeonameId());
        assertEquals("Bergen", first.getName());
        assertEquals("3162656", first.getFylkeGeonameId());
    }

    @Test
    void test_if_geonames_kommune_by_geoname_id_query_returns_correct_kommune() {
        GeonamesKommune result = graphQlTester.documentName("geonames-kommune-by-geoname-id")
                .variable("geonameId", "3141558")
                .execute()
                .path("$['data']['geonamesKommuneByGeonameId']")
                .entity(GeonamesKommune.class)
                .get();

        assertEquals("3141558", result.getGeonameId());
        assertEquals("Stavanger", result.getName());
        assertEquals("607872", result.getFylkeGeonameId());
    }

    @Test
    void test_if_geonames_kommune_by_geoname_id_unknown_returns_null() {
        graphQlTester.documentName("geonames-kommune-by-geoname-id")
                .variable("geonameId", "unknown")
                .execute()
                .path("$['data']['geonamesKommuneByGeonameId']")
                .valueIsNull();
    }
}
