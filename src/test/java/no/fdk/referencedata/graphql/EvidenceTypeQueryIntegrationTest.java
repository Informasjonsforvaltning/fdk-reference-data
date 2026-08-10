package no.fdk.referencedata.graphql;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.digdir.evidencetype.EvidenceType;
import no.fdk.referencedata.eu.eurovoc.EuroVoc;
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
class EvidenceTypeQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        HarvestTestSupport.harvest(registry, "evidence-type");
    }

    @Test
    void test_if_evidence_types_query_returns_all_evidence_types() {
        List<EvidenceType> result = graphQlTester.documentName("evidence-types")
                .execute()
                .path("$['data']['evidenceTypes']")
                .entityList(EvidenceType.class)
                .get();
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

}
