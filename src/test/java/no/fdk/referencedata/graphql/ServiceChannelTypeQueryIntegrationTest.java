package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.digdir.servicechanneltype.LocalServiceChannelTypeHarvester;
import no.fdk.referencedata.digdir.servicechanneltype.ServiceChannelTypeRepository;
import no.fdk.referencedata.digdir.servicechanneltype.ServiceChannelTypeService;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
                "wiremock.host=dummy",
                "wiremock.port=0"
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class ServiceChannelTypeQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ServiceChannelTypeRepository serviceChannelTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private GraphQLTestTemplate template;

    @BeforeEach
    public void setup() {
        ServiceChannelTypeService serviceChannelTypeService = new ServiceChannelTypeService(
                new LocalServiceChannelTypeHarvester("1"),
                serviceChannelTypeRepository,
                harvestSettingsRepository);

        serviceChannelTypeService.harvestAndSave(false);
    }

    @Test
    void test_if_service_channel_types_query_returns_all_service_channel_types() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/service-channel-types.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.norge.no/vocabulary/service-channel-type#assistant", response.get("$['data']['serviceChannelTypes'][0]['uri']"));
        assertEquals("assistant", response.get("$['data']['serviceChannelTypes'][0]['code']"));
        assertEquals("assistant", response.get("$['data']['serviceChannelTypes'][0]['label']['en']"));
    }

    @Test
    void test_if_service_channel_type_by_code_public_query_returns_public_service_channel_type() throws IOException {
        GraphQLResponse response = template.perform("graphql/service-channel-type-by-code.graphql",
                mapper.valueToTree(Map.of("code", "telephone")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.norge.no/vocabulary/service-channel-type#telephone", response.get("$['data']['serviceChannelTypeByCode']['uri']"));
        assertEquals("telephone", response.get("$['data']['serviceChannelTypeByCode']['code']"));
        assertEquals("telephone", response.get("$['data']['serviceChannelTypeByCode']['label']['en']"));
    }

    @Test
    void test_if_service_channel_type_by_code_unknown_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/service-channel-type-by-code.graphql",
                mapper.valueToTree(Map.of("code", "unknown")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['serviceChannelTypeByCode']"));
    }

}
