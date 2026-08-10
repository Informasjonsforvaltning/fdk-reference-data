package no.fdk.referencedata;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
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
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StandardCodeListGraphQlIT extends AbstractContainerTest {

    private static final String UNKNOWN_CODE = "__missing-code__";

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

    @Autowired
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    private boolean harvested;

    @BeforeEach
    void setup() {
        if (!harvested) {
            HarvestTestSupport.harvest(registry, CONTRACT_MODULE_IDS);
            harvested = true;
        }
    }

    static Stream<StandardCodeListGraphQlContractCase> standardCases() {
        return Stream.of(
                caseOf("access-right", "accessRights", "accessRightByCode", ACCESS_RIGHTS_SIZE, "CONFIDENTIAL"),
                caseOf("file-type", "fileTypes", "fileTypeByCode", FILE_TYPES_SIZE, "7Z"),
                caseOf("data-theme", "dataThemes", "dataThemeByCode", DATA_THEMES_SIZE, "AGRI"),
                caseOf("eurovoc", "euroVocs", "euroVocByCode", EUROVOCS_SIZE, "337"),
                caseOf("frequency", "frequencies", "frequencyByCode", FREQUENCIES_SIZE, "ANNUAL"),
                caseOf("distribution-status", "distributionStatuses", "distributionStatusByCode", DISTRIBUTION_STATUS_SIZE, "DEVELOP"),
                caseOf("distribution-type", "distributionTypes", "distributionTypeByCode", DISTRIBUTION_TYPES_SIZE, "DOWNLOADABLE_FILE"),
                caseOf("dataset-type", "datasetTypes", "datasetTypeByCode", DATASET_TYPES_SIZE, "NAL"),
                caseOf("main-activity", "mainActivities", "mainActivityByCode", MAIN_ACTIVITIES_SIZE, "health"),
                caseOf("concept-status", "conceptStatuses", "conceptStatusByCode", CONCEPT_STATUSES_SIZE, "CURRENT"),
                caseOf("planned-availability", "plannedAvailabilities", "plannedAvailabilityByCode", PLANNED_AVAILABILITY_SIZE, "TEMPORARY"),
                caseOf("currency", "currencies", "currencyByCode", CURRENCY_SIZE, "ISK"),
                caseOf("high-value-category", "highValueCategories", "highValueCategoryByCode", HIGH_VALUE_CATEGORIES_SIZE, "c_a9135398"),
                caseOf("continent", "continents", "continentByCode", CONTINENTS_SIZE, "EUROPE"),
                caseOf("country", "countries", "countryByCode", COUNTRIES_SIZE, "NOR"),
                caseOf("language", "languages", "languageByCode", LANGUAGES_SIZE, "NOB"),
                caseOf("concept-subject", "conceptSubjects", null, CONCEPT_SUBJECTS_SIZE, null),
                caseOf("evidence-type", "evidenceTypes", "evidenceTypeByCode", EVIDENCE_TYPES_SIZE, "certificate"),
                caseOf("service-channel-type", "serviceChannelTypes", "serviceChannelTypeByCode", SERVICE_CHANNEL_TYPES_SIZE, "telephone"),
                caseOf("role-type", "roleTypes", "roleTypeByCode", ROLE_TYPES_SIZE, "service-producer"),
                caseOf("audience-type", "audienceTypes", "audienceTypeByCode", AUDIENCE_TYPES_SIZE, "public"),
                caseOf("relationship-with-source-type", "relationshipWithSourceTypes", "relationshipWithSourceTypeByCode", RELATIONSHIP_WITH_SOURCE_TYPES_SIZE, "derived-from-source"),
                caseOf("quality-dimension", "qualityDimensions", "qualityDimensionByCode", QUALITY_DIMENSIONS_SIZE, "completeness"),
                caseOf("legal-resource-type", "legalResourceTypes", "legalResourceTypeByCode", LEGAL_RESOURCE_TYPES_SIZE, "regulation"),
                caseOf("mobility-theme", "mobilityThemes", "mobilityThemeByCode", MOBILITY_THEMES_SIZE, "speed-limits"),
                caseOf("mobility-condition", "mobilityConditions", "mobilityConditionByCode", MOBILITY_CONDITIONS_SIZE, "other"),
                caseOf("mobility-data-standard", "mobilityDataStandards", "mobilityDataStandardByCode", MOBILITY_DATA_STANDARDS_SIZE, "gml")
        );
    }

    private static StandardCodeListGraphQlContractCase caseOf(
            String moduleId, String listField, String byCodeField, int expectedSize, String sampleCode) {
        return new StandardCodeListGraphQlContractCase(moduleId, listField, byCodeField, expectedSize, sampleCode);
    }

    @ParameterizedTest(name = "{0} list size")
    @MethodSource("standardCases")
    void listQueryReturnsExpectedSize(StandardCodeListGraphQlContractCase contractCase) {
        String document = """
                query {
                  %s {
                    uri
                    code
                  }
                }
                """.formatted(contractCase.listField());

        List<Map> result = graphQlTester.document(document)
                .execute()
                .path("$['data']['%s']".formatted(contractCase.listField()))
                .entityList(Map.class)
                .get();

        assertEquals(contractCase.expectedSize(), result.size());
    }

    @ParameterizedTest(name = "{0} by known code")
    @MethodSource("standardCases")
    void byCodeQueryReturnsKnownCode(StandardCodeListGraphQlContractCase contractCase) {
        assumeTrue(contractCase.supportsByCode());

        String document = """
                query($code: String!) {
                  %s(code: $code) {
                    uri
                    code
                  }
                }
                """.formatted(contractCase.byCodeField());

        Map result = graphQlTester.document(document)
                .variable("code", contractCase.sampleCode())
                .execute()
                .path("$['data']['%s']".formatted(contractCase.byCodeField()))
                .entity(Map.class)
                .get();

        assertEquals(contractCase.sampleCode(), result.get("code"));
    }

    @ParameterizedTest(name = "{0} by unknown code")
    @MethodSource("standardCases")
    void byCodeQueryReturnsNullForUnknown(StandardCodeListGraphQlContractCase contractCase) {
        assumeTrue(contractCase.supportsByCode());

        String document = """
                query($code: String!) {
                  %s(code: $code) {
                    uri
                    code
                  }
                }
                """.formatted(contractCase.byCodeField());

        graphQlTester.document(document)
                .variable("code", UNKNOWN_CODE)
                .execute()
                .path("$['data']['%s']".formatted(contractCase.byCodeField()))
                .valueIsNull();
    }
}
