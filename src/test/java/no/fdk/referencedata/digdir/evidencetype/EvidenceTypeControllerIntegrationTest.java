package no.fdk.referencedata.digdir.evidencetype;

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
import org.springframework.web.client.RestClient;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

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

    @LocalServerPort
    private int port;

    @Autowired
    private EvidenceTypeRepository evidenceTypeRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        EvidenceTypeService evidenceTypeService = new EvidenceTypeService(
                new LocalEvidenceTypeHarvester("1"),
                evidenceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        evidenceTypeService.harvestAndSave(true);
    }

    @Test
    public void test_if_get_all_evidence_types_returns_valid_response() {
        EvidenceTypes evidenceTypes =
                restClient.get().uri("/digdir/evidence-types").retrieve().body(EvidenceTypes.class);

        assertEquals(4, evidenceTypes.getEvidenceTypes().size());

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
    public void test_if_post_evidence_types_fails_without_api_key() {
        assertEquals(4, evidenceTypeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.EVIDENCE_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = restClient.post().uri("/digdir/evidence-types")
                .headers(h -> h.addAll(headers)).exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(4, evidenceTypeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.EVIDENCE_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_evidence_types_executes_a_force_update() {
        assertEquals(4, evidenceTypeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.EVIDENCE_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = restClient.post().uri("/digdir/evidence-types")
                .headers(h -> h.addAll(headers)).exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(4, evidenceTypeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.EVIDENCE_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }

    @Test
    public void test_evidence_types_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/digdir/evidence-types", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(EvidenceTypeControllerIntegrationTest.class.getClassLoader().getResource("evidence-type.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
