package no.fdk.referencedata.digdir.roletype;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
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
public class RoleTypeControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RoleTypeRepository roleTypeRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        RoleTypeService roleTypeService = new RoleTypeService(
                LocalHarvesters.roleType(),
                roleTypeRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        roleTypeService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_role_types_returns_valid_response() {
        RoleTypes roleTypes =
                restClient.get().uri("/digdir/role-types").retrieve().body(RoleTypes.class);
        RoleType first = roleTypes.getRoleTypes().get(0);
        assertEquals("https://data.norge.no/vocabulary/role-type#data-consumer", first.getUri());
        assertEquals("data-consumer", first.getCode());
        assertEquals("data consumer", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_role_type_by_code_returns_valid_response() {
        RoleType roleType =
                restClient.get().uri("/digdir/role-types/service-producer").retrieve().body(RoleType.class);

        assertNotNull(roleType);
        assertEquals("https://data.norge.no/vocabulary/role-type#service-producer", roleType.getUri());
        assertEquals("service-producer", roleType.getCode());
        assertEquals("service producer", roleType.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_role_types_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/digdir/role-types", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(RoleTypeControllerIntegrationTest.class.getClassLoader().getResource("role-type.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
