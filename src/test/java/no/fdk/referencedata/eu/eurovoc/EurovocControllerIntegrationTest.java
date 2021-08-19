package no.fdk.referencedata.eu.eurovoc;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.mongo.AbstractMongoDbContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = { "scheduling.enabled=false" })
public class EurovocControllerIntegrationTest extends AbstractMongoDbContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private EurovocRepository EurovocRepository;

    @Autowired
    private EurovocSettingsRepository EurovocSettingsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        EurovocService eurovocService = new EurovocService(
                new LocalEurovocHarvester("1"),
                EurovocRepository,
                EurovocSettingsRepository);

        eurovocService.harvest();
    }

    @Test
    public void test_if_get_eurovoc_by_code_returns_valid_response() {
        Eurovoc eurovoc =
                this.restTemplate.getForObject("http://localhost:" + port + "/eu/eurovoc/5548", Eurovoc.class);

        assertNotNull(eurovoc);
        assertEquals("http://eurovoc.europa.eu/5548", eurovoc.getUri());
        assertEquals("5548", eurovoc.getCode());
        assertEquals("interinstitutional cooperation (EU)", eurovoc.getLabel().get(Language.ENGLISH.code()));
    }
}
