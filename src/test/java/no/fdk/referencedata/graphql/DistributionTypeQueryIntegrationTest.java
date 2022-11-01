package no.fdk.referencedata.graphql;

import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.eu.distributiontype.DistributionTypeRepository;
import no.fdk.referencedata.eu.distributiontype.DistributionTypeService;
import no.fdk.referencedata.eu.distributiontype.LocalDistributionTypeHarvester;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class DistributionTypeQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private GraphQLTestTemplate template;

    @Autowired
    private DistributionTypeRepository distributionTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @BeforeEach
    public void setup() {
        DistributionTypeService distributionTypeService = new DistributionTypeService(
                new LocalDistributionTypeHarvester("1"),
                distributionTypeRepository,
                harvestSettingsRepository);

        distributionTypeService.harvestAndSave(false);
    }

    @Test
    void test_if_distribution_types_query_returns_all_distribution_types() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/distribution-types.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://publications.europa.eu/resource/authority/distribution-type/DOWNLOADABLE_FILE", response.get("$['data']['distributionTypes'][0]['uri']"));
        assertEquals("DOWNLOADABLE_FILE", response.get("$['data']['distributionTypes'][0]['code']"));
        assertEquals("Downloadable file", response.get("$['data']['distributionTypes'][0]['label']['en']"));
    }

    @Test
    void test_if_distribution_type_by_code_aac_query_returns_econ_distribution_type() throws IOException {
        GraphQLResponse response = template.perform("graphql/distribution-type-by-code.graphql",
                mapper.valueToTree(Map.of("code", "FEED_INFO")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://publications.europa.eu/resource/authority/distribution-type/FEED_INFO", response.get("$['data']['distributionTypeByCode']['uri']"));
        assertEquals("FEED_INFO", response.get("$['data']['distributionTypeByCode']['code']"));
        assertEquals("Information feed", response.get("$['data']['distributionTypeByCode']['label']['en']"));
    }

    @Test
    void test_if_distribution_type_by_code_unknown_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/distribution-type-by-code.graphql",
                mapper.valueToTree(Map.of("code", "unknown")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['distributionTypeByCode']"));
    }

}
