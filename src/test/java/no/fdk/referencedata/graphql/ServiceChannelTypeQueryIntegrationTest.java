package no.fdk.referencedata.graphql;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.digdir.servicechanneltype.ServiceChannelType;
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
class ServiceChannelTypeQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        HarvestTestSupport.harvest(registry, "service-channel-type");
    }

    @Test
    void test_if_service_channel_types_query_returns_all_service_channel_types() {
        List<ServiceChannelType> result = graphQlTester.documentName("service-channel-types")
                .execute()
                .path("$['data']['serviceChannelTypes']")
                .entityList(ServiceChannelType.class)
                .get();
        ServiceChannelType serviceChannelType = result.get(0);

        assertEquals("https://data.norge.no/vocabulary/service-channel-type#assistant", serviceChannelType.getUri());
        assertEquals("assistant", serviceChannelType.getCode());
        assertEquals("assistent", serviceChannelType.getLabel().get("nb"));
        assertEquals("assistant", serviceChannelType.getLabel().get("en"));
    }

    @Test
    void test_if_service_channel_type_by_code_public_query_returns_public_service_channel_type() {
        ServiceChannelType result = graphQlTester.documentName("service-channel-type-by-code")
                .variable("code", "telephone")
                .execute()
                .path("$['data']['serviceChannelTypeByCode']")
                .entity(ServiceChannelType.class)
                .get();

        assertEquals("https://data.norge.no/vocabulary/service-channel-type#telephone", result.getUri());
        assertEquals("telephone", result.getCode());
        assertEquals("telefon", result.getLabel().get("nb"));
        assertEquals("telephone", result.getLabel().get("en"));
    }

}
