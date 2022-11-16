package no.fdk.referencedata.adms;

import no.fdk.referencedata.adms.status.ADMSStatus;
import no.fdk.referencedata.adms.status.ADMSStatuses;
import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "scheduling.enabled=false",
        })
@ActiveProfiles("test")
public class StatusControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test_if_get_all_statuses_returns_valid_response() {
        ADMSStatuses statuses =
                this.restTemplate.getForObject("http://localhost:" + port + "/adms/statuses", ADMSStatuses.class);

        assertEquals(4, statuses.getStatuses().size());

        ADMSStatus status = statuses.getStatuses().get(1);
        assertEquals("http://purl.org/adms/status/Deprecated", status.getUri());
        assertEquals("Deprecated", status.getCode());
        assertEquals("Deprecated", status.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_by_code_returns_valid_response() {
        ADMSStatus status =
                this.restTemplate.getForObject("http://localhost:" + port + "/adms/statuses/UnderDevelopment", ADMSStatus.class);

        assertNotNull(status);
        assertEquals("http://purl.org/adms/status/UnderDevelopment", status.getUri());
        assertEquals("UnderDevelopment", status.getCode());
        assertEquals("Under development", status.getLabel().get(Language.ENGLISH.code()));
    }
}
