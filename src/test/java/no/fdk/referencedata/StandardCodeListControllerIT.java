package no.fdk.referencedata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.util.Set;
import java.util.stream.Stream;

import static no.fdk.referencedata.LocalHarvestFixtures.ACCESS_RIGHTS_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.AUDIENCE_TYPES_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.CONCEPT_STATUSES_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.CONCEPT_SUBJECTS_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.CONTINENTS_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.COUNTRIES_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.CURRENCY_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.DATASET_TYPES_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.DATA_THEMES_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.DISTRIBUTION_STATUS_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.DISTRIBUTION_TYPES_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.EUROVOCS_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.EVIDENCE_TYPES_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.FILE_TYPES_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.FREQUENCIES_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.HIGH_VALUE_CATEGORIES_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.LANGUAGES_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.LEGAL_RESOURCE_TYPES_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.LICENCES_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.MAIN_ACTIVITIES_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.MOBILITY_CONDITIONS_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.MOBILITY_DATA_STANDARDS_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.MOBILITY_THEMES_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.PLANNED_AVAILABILITY_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.QUALITY_DIMENSIONS_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.RELATIONSHIP_WITH_SOURCE_TYPES_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.ROLE_TYPES_SIZE;
import static no.fdk.referencedata.LocalHarvestFixtures.SERVICE_CHANNEL_TYPES_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
                "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StandardCodeListControllerIT extends AbstractContainerTest {

    private static final String UNKNOWN_CODE = "__missing-code__";
    private static final MediaType TURTLE = MediaType.parseMediaType("text/turtle");

    private static final Set<String> CONTRACT_MODULE_IDS = Set.of(
            "access-right",
            "file-type",
            "data-theme",
            "eurovoc",
            "frequency",
            "distribution-status",
            "distribution-type",
            "dataset-type",
            "main-activity",
            "concept-status",
            "planned-availability",
            "currency",
            "licence",
            "high-value-category",
            "continent",
            "country",
            "language",
            "concept-subject",
            "evidence-type",
            "service-channel-type",
            "role-type",
            "audience-type",
            "relationship-with-source-type",
            "quality-dimension",
            "legal-resource-type",
            "mobility-theme",
            "mobility-condition",
            "mobility-data-standard"
    );

    @LocalServerPort
    private int port;

    @Autowired
    private ReferenceDataRegistry registry;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RestClient restClient;
    private boolean harvested;

    @BeforeEach
    void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        if (!harvested) {
            HarvestTestSupport.harvest(registry, CONTRACT_MODULE_IDS);
            harvested = true;
        }
    }

    static Stream<StandardCodeListContractCase> standardCases() {
        return Stream.of(
                caseOf("access-right", "/eu/access-rights", "accessRights", ACCESS_RIGHTS_SIZE, "CONFIDENTIAL"),
                caseOf("file-type", "/eu/file-types", "fileTypes", FILE_TYPES_SIZE, "7Z"),
                caseOf("data-theme", "/eu/data-themes", "dataThemes", DATA_THEMES_SIZE, "AGRI"),
                caseOf("eurovoc", "/eu/eurovocs", "euroVocs", EUROVOCS_SIZE, "337"),
                caseOf("frequency", "/eu/frequencies", "frequencies", FREQUENCIES_SIZE, "ANNUAL"),
                caseOf("distribution-status", "/eu/distribution-statuses", "distributionStatuses", DISTRIBUTION_STATUS_SIZE, "DEVELOP"),
                caseOf("distribution-type", "/eu/distribution-types", "distributionTypes", DISTRIBUTION_TYPES_SIZE, "DOWNLOADABLE_FILE"),
                caseOf("dataset-type", "/eu/dataset-types", "datasetTypes", DATASET_TYPES_SIZE, "NAL"),
                caseOf("main-activity", "/eu/main-activities", "mainActivities", MAIN_ACTIVITIES_SIZE, "health"),
                caseOf("concept-status", "/eu/concept-statuses", "conceptStatuses", CONCEPT_STATUSES_SIZE, "CURRENT"),
                caseOf("planned-availability", "/eu/planned-availabilities", "plannedAvailabilities", PLANNED_AVAILABILITY_SIZE, "TEMPORARY"),
                caseOf("currency", "/eu/currencies", "currencies", CURRENCY_SIZE, "ISK"),
                caseOf("licence", "/eu/licences", "licences", LICENCES_SIZE, "CC0"),
                caseOf("high-value-category", "/eu/high-value-categories", "highValueCategories", HIGH_VALUE_CATEGORIES_SIZE, "c_a9135398"),
                caseOf("continent", "/eu/continents", "continents", CONTINENTS_SIZE, "EUROPE"),
                caseOf("country", "/eu/countries", "countries", COUNTRIES_SIZE, "NOR"),
                caseOf("language", "/eu/languages", "languages", LANGUAGES_SIZE, "NOB"),
                caseOf("concept-subject", "/digdir/concept-subjects", "conceptSubjects", CONCEPT_SUBJECTS_SIZE, null),
                caseOf("evidence-type", "/digdir/evidence-types", "evidenceTypes", EVIDENCE_TYPES_SIZE, "certificate"),
                caseOf("service-channel-type", "/digdir/service-channel-types", "serviceChannelTypes", SERVICE_CHANNEL_TYPES_SIZE, "telephone"),
                caseOf("role-type", "/digdir/role-types", "roleTypes", ROLE_TYPES_SIZE, "service-producer"),
                caseOf("audience-type", "/digdir/audience-types", "audienceTypes", AUDIENCE_TYPES_SIZE, "public"),
                caseOf("relationship-with-source-type", "/digdir/relationship-with-source-types", "relationshipWithSourceTypes", RELATIONSHIP_WITH_SOURCE_TYPES_SIZE, "derived-from-source"),
                caseOf("quality-dimension", "/digdir/quality-dimensions", "qualityDimensions", QUALITY_DIMENSIONS_SIZE, "completeness"),
                caseOf("legal-resource-type", "/digdir/legal-resource-types", "legalResourceTypes", LEGAL_RESOURCE_TYPES_SIZE, "regulation"),
                caseOf("mobility-theme", "/mobility/themes", "mobilityThemes", MOBILITY_THEMES_SIZE, "speed-limits"),
                caseOf("mobility-condition", "/mobility/conditions-for-access-and-usage", "mobilityConditions", MOBILITY_CONDITIONS_SIZE, "other"),
                caseOf("mobility-data-standard", "/mobility/data-standards", "mobilityDataStandards", MOBILITY_DATA_STANDARDS_SIZE, "gml")
        );
    }

    private static StandardCodeListContractCase caseOf(
            String moduleId, String restPath, String jsonArrayField, int expectedSize, String sampleCode) {
        return new StandardCodeListContractCase(moduleId, restPath, jsonArrayField, expectedSize, sampleCode);
    }

    @ParameterizedTest(name = "{0} GET list size")
    @MethodSource("standardCases")
    void getListReturnsExpectedSize(StandardCodeListContractCase contractCase) throws Exception {
        String body = restClient.get()
                .uri(contractCase.restPath())
                .retrieve()
                .body(String.class);

        JsonNode array = objectMapper.readTree(body).get(contractCase.jsonArrayField());
        assertNotNull(array, "missing wrapper field " + contractCase.jsonArrayField());
        assertTrue(array.isArray());
        assertEquals(contractCase.expectedSize(), array.size());
    }

    @ParameterizedTest(name = "{0} GET by known code")
    @MethodSource("standardCases")
    void getByKnownCodeReturnsOk(StandardCodeListContractCase contractCase) throws Exception {
        assumeTrue(contractCase.supportsByCode());

        ResponseEntity<String> response = restClient.get()
                .uri(contractCase.restPath() + "/" + contractCase.sampleCode())
                .exchange((request, clientResponse) -> ResponseEntity
                        .status(clientResponse.getStatusCode())
                        .body(new String(clientResponse.getBody().readAllBytes())));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode item = objectMapper.readTree(response.getBody());
        assertEquals(contractCase.sampleCode(), item.get("code").asText());
    }

    @ParameterizedTest(name = "{0} GET by unknown code")
    @MethodSource("standardCases")
    void getByUnknownCodeReturnsNotFound(StandardCodeListContractCase contractCase) {
        assumeTrue(contractCase.supportsByCode());

        ResponseEntity<Void> response = restClient.get()
                .uri(contractCase.restPath() + "/" + UNKNOWN_CODE)
                .exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @ParameterizedTest(name = "{0} POST without API key")
    @MethodSource("standardCases")
    void postWithoutApiKeyIsForbidden(StandardCodeListContractCase contractCase) {
        long before = countFor(contractCase);

        ResponseEntity<Void> response = restClient.post()
                .uri(contractCase.restPath())
                .header("X-API-KEY", "")
                .exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(before, countFor(contractCase));
    }

    @ParameterizedTest(name = "{0} POST with API key")
    @MethodSource("standardCases")
    void postWithApiKeySucceeds(StandardCodeListContractCase contractCase) {
        long before = countFor(contractCase);

        ResponseEntity<Void> response = restClient.post()
                .uri(contractCase.restPath())
                .header("X-API-KEY", "my-api-key")
                .exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(before, countFor(contractCase));
    }

    @ParameterizedTest(name = "{0} GET turtle")
    @MethodSource("standardCases")
    void getTurtleWhenSupported(StandardCodeListContractCase contractCase) {
        ResponseEntity<String> response = restClient.get()
                .uri(contractCase.restPath())
                .accept(TURTLE)
                .exchange((request, clientResponse) -> {
                    MediaType contentType = clientResponse.getHeaders().getContentType();
                    String body = clientResponse.getBody() == null
                            ? ""
                            : new String(clientResponse.getBody().readAllBytes());
                    return ResponseEntity.status(clientResponse.getStatusCode())
                            .contentType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM)
                            .body(body);
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getContentType().isCompatibleWith(TURTLE));
        assertFalse(response.getBody() == null || response.getBody().isBlank());
    }

    private long countFor(StandardCodeListContractCase contractCase) {
        return registry.withApi().stream()
                .filter(module -> module.id().equals(contractCase.moduleId()))
                .findFirst()
                .orElseThrow()
                .api()
                .findAllSorted()
                .size();
    }
}
