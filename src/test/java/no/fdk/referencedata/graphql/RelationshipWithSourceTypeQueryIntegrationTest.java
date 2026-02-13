package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;

import no.fdk.referencedata.digdir.relationshipwithsourcetype.LocalRelationshipWithSourceTypeHarvester;
import no.fdk.referencedata.digdir.relationshipwithsourcetype.RelationshipWithSourceType;
import no.fdk.referencedata.digdir.relationshipwithsourcetype.RelationshipWithSourceTypeRepository;
import no.fdk.referencedata.digdir.relationshipwithsourcetype.RelationshipWithSourceTypeService;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.referencetypes.ReferenceType;
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
class RelationshipWithSourceTypeQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private RelationshipWithSourceTypeRepository relationshipWithSourceTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        RelationshipWithSourceTypeService relationshipWithSourceTypeService = new RelationshipWithSourceTypeService(
                new LocalRelationshipWithSourceTypeHarvester("1"),
                relationshipWithSourceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        relationshipWithSourceTypeService.harvestAndSave(false);
    }

    @Test
    void test_if_relationship_with_source_types_query_returns_all_relationship_with_source_types() {
        List<RelationshipWithSourceType> result = graphQlTester.documentName("relationship-with-source-types")
                .execute()
                .path("$['data']['relationshipWithSourceTypes']")
                .entityList(RelationshipWithSourceType.class)
                .get();

        assertEquals(3, result.size());

        RelationshipWithSourceType relationshipWithSourceType = result.get(0);

        assertEquals("https://data.norge.no/vocabulary/relationship-with-source-type#derived-from-source", relationshipWithSourceType.getUri());
        assertEquals("derived-from-source", relationshipWithSourceType.getCode());
        assertEquals("basert på kilde", relationshipWithSourceType.getLabel().get("nb"));
        assertEquals("basert på kjelde", relationshipWithSourceType.getLabel().get("nn"));
        assertEquals("derived from source", relationshipWithSourceType.getLabel().get("en"));
    }

    @Test
    void test_if_relationship_with_source_type_by_code_derived_from_source_query_returns_derived_from_source_relationship_with_source_type() {
        RelationshipWithSourceType result = graphQlTester.documentName("relationship-with-source-type-by-code")
                .variable("code", "derived-from-source")
                .execute()
                .path("$['data']['relationshipWithSourceTypeByCode']")
                .entity(RelationshipWithSourceType.class)
                .get();

        assertEquals("https://data.norge.no/vocabulary/relationship-with-source-type#derived-from-source", result.getUri());
        assertEquals("derived-from-source", result.getCode());
        assertEquals("basert på kilde", result.getLabel().get("nb"));
        assertEquals("basert på kjelde", result.getLabel().get("nn"));
        assertEquals("derived from source", result.getLabel().get("en"));
    }

    @Test
    void test_if_relationship_with_source_type_by_code_unknown_query_returns_null() {
        graphQlTester.documentName("relationship-with-source-type-by-code")
                .variable("code", "unknown")
                .execute()
                .path("$['data']['relationshipWithSourceTypeByCode']")
                .valueIsNull();
    }
}
