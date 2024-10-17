package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.digdir.evidencetype.EvidenceType;
import no.fdk.referencedata.digdir.evidencetype.EvidenceTypeRepository;
import no.fdk.referencedata.digdir.evidencetype.EvidenceTypeService;
import no.fdk.referencedata.digdir.evidencetype.LocalEvidenceTypeHarvester;
import no.fdk.referencedata.eu.eurovoc.EuroVoc;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class EvidenceTypeQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private EvidenceTypeRepository evidenceTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        EvidenceTypeService evidenceTypeService = new EvidenceTypeService(
                new LocalEvidenceTypeHarvester("1"),
                evidenceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        evidenceTypeService.harvestAndSave(false);
    }

    @Test
    void test_if_evidence_types_query_returns_all_evidence_types() {
        List<EvidenceType> result = graphQlTester.documentName("evidence-types")
                .execute()
                .path("$['data']['evidenceTypes']")
                .entityList(EvidenceType.class)
                .get();

        Assertions.assertEquals(4, result.size());

        EvidenceType evidenceType = result.get(0);

        assertEquals("https://data.norge.no/vocabulary/evidence-type#attestation", evidenceType.getUri());
        assertEquals("attestation", evidenceType.getCode());
        assertEquals("attest", evidenceType.getLabel().get("nb"));
        assertEquals("attestation", evidenceType.getLabel().get("en"));
    }

    @Test
    void test_if_evidence_type_by_code_public_query_returns_public_evidence_type() {
        EuroVoc result = graphQlTester.documentName("evidence-type-by-code")
                .variable("code", "protocol")
                .execute()
                .path("$['data']['evidenceTypeByCode']")
                .entity(EuroVoc.class)
                .get();

        assertEquals("https://data.norge.no/vocabulary/evidence-type#protocol", result.getUri());
        assertEquals("protocol", result.getCode());
        assertEquals("protokoll", result.getLabel().get("nb"));
        assertEquals("protocol", result.getLabel().get("en"));
    }

    @Test
    void test_if_evidence_type_by_code_unknown_query_returns_null() {
        graphQlTester.documentName("evidence-type-by-code")
                .variable("code", "unknown")
                .execute()
                .path("$['data']['evidenceTypeByCode']")
                .valueIsNull();
    }

}
