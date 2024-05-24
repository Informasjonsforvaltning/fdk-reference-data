package no.fdk.referencedata.digdir.relationshipwithsourcetype;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class RelationshipWithSourceTypeControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RelationshipWithSourceTypeRepository relationshipWithSourceTypeRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        RelationshipWithSourceTypeService relationshipWithSourceTypeService = new RelationshipWithSourceTypeService(
                new LocalRelationshipWithSourceTypeHarvester("1"),
                relationshipWithSourceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        relationshipWithSourceTypeService.harvestAndSave(true);
    }

    @Test
    public void test_if_get_all_relationshipWithSource_types_returns_valid_response() {
        RelationshipWithSourceTypes relationshipWithSourceTypes =
                this.restTemplate.getForObject("http://localhost:" + port + "/digdir/relationship-with-source-types", RelationshipWithSourceTypes.class);

        assertEquals(3, relationshipWithSourceTypes.getRelationshipWithSourceTypes().size());

        RelationshipWithSourceType first = relationshipWithSourceTypes.getRelationshipWithSourceTypes().get(0);
        assertEquals("https://data.norge.no/vocabulary/relationship-with-source-type#derived-from-source", first.getUri());
        assertEquals("derived-from-source", first.getCode());
        assertEquals("derived from source", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_relationshipWithSource_type_by_code_returns_valid_response() {
        RelationshipWithSourceType relationshipWithSourceType =
                this.restTemplate.getForObject("http://localhost:" + port + "/digdir/relationship-with-source-types/derived-from-source", RelationshipWithSourceType.class);

        assertNotNull(relationshipWithSourceType);
        assertEquals("https://data.norge.no/vocabulary/relationship-with-source-type#derived-from-source", relationshipWithSourceType.getUri());
        assertEquals("derived-from-source", relationshipWithSourceType.getCode());
        assertEquals("derived from source", relationshipWithSourceType.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_post_relationshipWithSource_types_fails_without_api_key() {
        assertEquals(3, relationshipWithSourceTypeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.RELATIONSHIP_WITH_SOURCE_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/digdir/relationship-with-source-types",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(3, relationshipWithSourceTypeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.RELATIONSHIP_WITH_SOURCE_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_relationship_with_source_types_executes_a_force_update() {
        assertEquals(3, relationshipWithSourceTypeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.RELATIONSHIP_WITH_SOURCE_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/digdir/relationship-with-source-types",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, relationshipWithSourceTypeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.RELATIONSHIP_WITH_SOURCE_TYPE.name()).orElseThrow();
        assertEquals("2023-01-19", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }

    @Test
    public void test_relationship_with_source_types_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/digdir/relationship-with-source-types", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(RelationshipWithSourceTypeControllerIntegrationTest.class.getClassLoader().getResource("relationship-with-source-type.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
