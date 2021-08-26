package no.fdk.referencedata.eu.eurovoc;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.mongo.AbstractMongoDbContainerTest;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class EuroVocControllerIntegrationTest extends AbstractMongoDbContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private EuroVocRepository euroVocRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        EuroVocService EuroVocService = new EuroVocService(
                new LocalEuroVocHarvester("1"),
                euroVocRepository,
                harvestSettingsRepository);

        EuroVocService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_eurovocs_returns_valid_response() {
        EuroVocs euroVocs =
                this.restTemplate.getForObject("http://localhost:" + port + "/eu/eurovocs", EuroVocs.class);

        assertEquals(7322, euroVocs.getEuroVocs().size());

        EuroVoc first = euroVocs.getEuroVocs().get(0);
        assertEquals("http://eurovoc.europa.eu/1", first.getUri());
        assertEquals("1", first.getCode());
        assertEquals("Århus (county)", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_eurovoc_by_code_returns_valid_response() {
        EuroVoc euroVoc =
                this.restTemplate.getForObject("http://localhost:" + port + "/eu/eurovocs/5548", EuroVoc.class);

        assertNotNull(euroVoc);
        assertEquals("http://eurovoc.europa.eu/5548", euroVoc.getUri());
        assertEquals("5548", euroVoc.getCode());
        assertEquals("interinstitutional cooperation (EU)", euroVoc.getLabel().get(Language.ENGLISH.code()));
    }
}
