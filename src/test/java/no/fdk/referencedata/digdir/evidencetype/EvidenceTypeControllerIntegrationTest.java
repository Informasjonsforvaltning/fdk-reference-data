package no.fdk.referencedata.digdir.evidencetype;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class EvidenceTypeControllerIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        HarvestTestSupport.harvest(registry, "evidence-type");
    }

    @Test
    public void test_if_get_all_evidence_types_returns_valid_response() {
        EvidenceTypes evidenceTypes =
                restClient.get().uri("/digdir/evidence-types").retrieve().body(EvidenceTypes.class);
        EvidenceType first = evidenceTypes.getEvidenceTypes().get(0);
        assertEquals("https://data.norge.no/vocabulary/evidence-type#attestation", first.getUri());
        assertEquals("attestation", first.getCode());
        assertEquals("attestation", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_evidence_type_by_code_returns_valid_response() {
        EvidenceType evidenceType =
                restClient.get().uri("/digdir/evidence-types/certificate").retrieve().body(EvidenceType.class);

        assertNotNull(evidenceType);
        assertEquals("https://data.norge.no/vocabulary/evidence-type#certificate", evidenceType.getUri());
        assertEquals("certificate", evidenceType.getCode());
        assertEquals("certificate", evidenceType.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_evidence_types_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/digdir/evidence-types", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(EvidenceTypeControllerIntegrationTest.class.getClassLoader().getResource("evidence-type.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
