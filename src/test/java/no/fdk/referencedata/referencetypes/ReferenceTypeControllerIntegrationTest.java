package no.fdk.referencedata.referencetypes;

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
public class ReferenceTypeControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test_if_get_all_reference_types_returns_valid_response() {
        ReferenceTypes referenceTypes =
                this.restTemplate.getForObject("http://localhost:" + port + "/reference-types", ReferenceTypes.class);

        assertEquals(11, referenceTypes.getReferenceTypes().size());

        ReferenceType first = referenceTypes.getReferenceTypes().get(0);
        assertEquals("http://purl.org/dc/terms/hasVersion", first.getUri());
        assertEquals("hasVersion", first.getCode());
        assertEquals("Has version", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_reference_type_by_code_returns_valid_response() {
        ReferenceType referenceType =
                this.restTemplate.getForObject("http://localhost:" + port + "/reference-types/isRequiredBy", ReferenceType.class);

        assertNotNull(referenceType);
        assertEquals("http://purl.org/dc/terms/isRequiredBy", referenceType.getUri());
        assertEquals("isRequiredBy", referenceType.getCode());
        assertEquals("Is required by", referenceType.getLabel().get(Language.ENGLISH.code()));
    }
}
