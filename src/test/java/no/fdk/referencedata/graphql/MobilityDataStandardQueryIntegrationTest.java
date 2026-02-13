package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.mobility.datastandard.LocalMobilityDataStandardHarvester;
import no.fdk.referencedata.mobility.datastandard.MobilityDataStandard;
import no.fdk.referencedata.mobility.datastandard.MobilityDataStandardRepository;
import no.fdk.referencedata.mobility.datastandard.MobilityDataStandardService;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
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
import static org.mockito.Mockito.mock;

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
class MobilityDataStandardQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private MobilityDataStandardRepository mobilityDataStandardRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        MobilityDataStandardService mobilityDataStandardService = new MobilityDataStandardService(
                new LocalMobilityDataStandardHarvester("1.1.0"),
                mobilityDataStandardRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        mobilityDataStandardService.harvestAndSave(false);
    }

    @Test
    void test_if_mobility_data_standards_query_returns_all_mobility_data_standards() {
        List<MobilityDataStandard> result = graphQlTester.documentName("mobility-data-standards")
                .execute()
                .path("$['data']['mobilityDataStandards']")
                .entityList(MobilityDataStandard.class)
                .get();

        assertEquals(15, result.size());

        MobilityDataStandard standard = result.get(0);

        assertEquals("https://w3id.org/mobilitydcat-ap/mobility-data-standard/c-its", standard.getUri());
        assertEquals("c-its", standard.getCode());
        assertEquals("C-ITS", standard.getLabel().get("en"));
    }

    @Test
    void test_if_mobility_data_standard_by_code_public_query_returns_correct_data_standard() {
        MobilityDataStandard result = graphQlTester.documentName("mobility-data-standard-by-code")
                .variable("code", "ocpi")
                .execute()
                .path("$['data']['mobilityDataStandardByCode']")
                .entity(MobilityDataStandard.class)
                .get();

        assertEquals("https://w3id.org/mobilitydcat-ap/mobility-data-standard/ocpi", result.getUri());
        assertEquals("ocpi", result.getCode());
        assertEquals("OCPI", result.getLabel().get("en"));
    }

    @Test
    void test_if_mobility_data_standard_by_code_unknown_query_returns_null() {
        graphQlTester.documentName("mobility-data-standard-by-code")
                .variable("code", "unknown")
                .execute()
                .path("$['data']['mobilityDataStandardByCode']")
                .valueIsNull();
    }

}
