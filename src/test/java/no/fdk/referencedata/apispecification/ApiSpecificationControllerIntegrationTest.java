package no.fdk.referencedata.apispecification;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "scheduling.enabled=false",
        })
@ActiveProfiles("test")
public class ApiSpecificationControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test_if_get_all_api_specifications_returns_valid_response() {
        ApiSpecifications apiSpecifications =
                this.restTemplate.getForObject("http://localhost:" + port + "/api-specifications", ApiSpecifications.class);

        assertEquals(2, apiSpecifications.getApiSpecifications().size());

        ApiSpecification first = apiSpecifications.getApiSpecifications().get(0);
        assertEquals("https://data.norge.no/reference-data/api-specifications/account", first.getUri());
        assertEquals("account", first.getCode());
        assertEquals("https://bitsnorge.github.io/dsop-accounts-api", first.getSource());
        assertEquals("Account details", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_api_specification_by_code_returns_valid_response() {
        ApiSpecification apiSpecification =
                this.restTemplate.getForObject("http://localhost:" + port + "/api-specifications/account", ApiSpecification.class);

        assertNotNull(apiSpecification);
        assertEquals("https://data.norge.no/reference-data/api-specifications/account", apiSpecification.getUri());
        assertEquals("account", apiSpecification.getCode());
        assertEquals("https://bitsnorge.github.io/dsop-accounts-api", apiSpecification.getSource());
        assertEquals("Account details", apiSpecification.getLabel().get(Language.ENGLISH.code()));
    }
}
