package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.eu.distributiontype.DistributionType;
import no.fdk.referencedata.eu.distributiontype.DistributionTypeRepository;
import no.fdk.referencedata.eu.distributiontype.DistributionTypeService;
import no.fdk.referencedata.eu.distributiontype.LocalDistributionTypeHarvester;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
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
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class DistributionTypeQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private DistributionTypeRepository distributionTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @BeforeEach
    public void setup() {
        DistributionTypeService distributionTypeService = new DistributionTypeService(
                new LocalDistributionTypeHarvester("1"),
                distributionTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        distributionTypeService.harvestAndSave(false);
    }

    @Test
    void test_if_distribution_types_query_returns_all_distribution_types() {
        List<DistributionType> result = graphQlTester.documentName("distribution-types")
                .execute()
                .path("$['data']['distributionTypes']")
                .entityList(DistributionType.class)
                .get();

        Assertions.assertEquals(4, result.size());

        DistributionType distributionType = result.get(0);

        assertEquals(
                "http://publications.europa.eu/resource/authority/distribution-type/DOWNLOADABLE_FILE",
                distributionType.getUri()
        );
        assertEquals("DOWNLOADABLE_FILE", distributionType.getCode());
        assertEquals("Nedlastbar fil", distributionType.getLabel().get("no"));
        assertEquals("Nedlastbar fil", distributionType.getLabel().get("nb"));
        assertEquals("Nedlastbar fil", distributionType.getLabel().get("nn"));
        assertEquals("Downloadable file", distributionType.getLabel().get("en"));
    }

    @Test
    void test_if_distribution_type_by_code_aac_query_returns_econ_distribution_type() {
        DistributionType result = graphQlTester.documentName("distribution-type-by-code")
                .variable("code", "FEED_INFO")
                .execute()
                .path("$['data']['distributionTypeByCode']")
                .entity(DistributionType.class)
                .get();

        assertEquals(
                "http://publications.europa.eu/resource/authority/distribution-type/FEED_INFO",
                result.getUri()
        );
        assertEquals("FEED_INFO", result.getCode());
        assertEquals("Informasjonsstrøm", result.getLabel().get("no"));
        assertEquals("Informasjonsstrøm", result.getLabel().get("nb"));
        assertEquals("Informasjonsstraum", result.getLabel().get("nn"));
        assertEquals("Information feed", result.getLabel().get("en"));
    }

    @Test
    void test_if_distribution_type_by_code_unknown_query_returns_null() {
        graphQlTester.documentName("distribution-type-by-code")
                .variable("code", "unknown")
                .execute()
                .path("$['data']['distributionTypeByCode']")
                .valueIsNull();
    }

}
