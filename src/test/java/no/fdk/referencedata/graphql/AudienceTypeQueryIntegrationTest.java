package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.digdir.audiencetype.AudienceType;
import no.fdk.referencedata.digdir.audiencetype.AudienceTypeRepository;
import no.fdk.referencedata.digdir.audiencetype.AudienceTypeService;
import no.fdk.referencedata.digdir.audiencetype.LocalAudienceTypeHarvester;
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
class AudienceTypeQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private AudienceTypeRepository audienceTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        AudienceTypeService audienceTypeService = new AudienceTypeService(
                new LocalAudienceTypeHarvester("1"),
                audienceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        audienceTypeService.harvestAndSave(false);
    }

    @Test
    void test_if_audience_types_query_returns_all_audience_types() {
        List<AudienceType> result = graphQlTester.documentName("audience-types")
                .execute()
                .path("$['data']['audienceTypes']")
                .entityList(AudienceType.class)
                .get();

        assertEquals(2, result.size());

        AudienceType audienceType = result.get(0);
        assertEquals("https://data.norge.no/vocabulary/audience-type#public", audienceType.getUri());
        assertEquals("public", audienceType.getCode());
        assertEquals("allmennheten", audienceType.getLabel().get("nb"));
        assertEquals("allmenta", audienceType.getLabel().get("nn"));
        assertEquals("public", audienceType.getLabel().get("en"));
    }

    @Test
    void test_if_audience_type_by_code_public_query_returns_public_audience_type() {
        AudienceType result = graphQlTester.documentName("audience-type-by-code")
                .variable("code", "specialist")
                .execute()
                .path("$['data']['audienceTypeByCode']")
                .entity(AudienceType.class)
                .get();

        assertEquals("https://data.norge.no/vocabulary/audience-type#specialist", result.getUri());
        assertEquals("specialist", result.getCode());
        assertEquals("spesialist", result.getLabel().get("nb"));
        assertEquals("spesialist", result.getLabel().get("nn"));
        assertEquals("specialist", result.getLabel().get("en"));
    }

    @Test
    void test_if_audience_type_by_code_unknown_query_returns_null() {
        graphQlTester.documentName("audience-type-by-code")
                .variable("code", "unknown")
                .execute()
                .path("$['data']['audienceTypeByCode']")
                .valueIsNull();
    }

}
