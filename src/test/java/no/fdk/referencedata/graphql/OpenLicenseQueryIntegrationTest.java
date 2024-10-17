package no.fdk.referencedata.graphql;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.openlicences.OpenLicense;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@ActiveProfiles("test")
class OpenLicenseQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void test_if_open_license_query_returns_valid_response() {
        List<OpenLicense> result = graphQlTester.documentName("open-licenses")
                .execute()
                .path("$['data']['openLicenses']")
                .entityList(OpenLicense.class)
                .get();

        assertEquals(3, result.size());

        OpenLicense license = result.get(1);

        assertEquals("http://publications.europa.eu/resource/authority/licence/CC_BY_4_0", license.getUri());
        assertEquals("CC BY 4.0", license.getCode());
        assertEquals("Creative Commons Navngivelse 4.0 Internasjonal", license.getLabel().get("no"));
        assertEquals("Creative Commons Attribution 4.0 International", license.getLabel().get("en"));
    }

    @Test
    void test_if_linguistic_system_by_code_query_returns_valid_response() {
        OpenLicense result = graphQlTester.documentName("open-license-by-code")
                .variable("code", "NLOD20")
                .execute()
                .path("$['data']['openLicenseByCode']")
                .entity(OpenLicense.class)
                .get();

        assertEquals("http://publications.europa.eu/resource/authority/licence/NLOD_2_0", result.getUri());
        assertEquals("NLOD20", result.getCode());
        assertEquals("Norsk lisens for offentlige data", result.getLabel().get("no"));
        assertEquals("Norwegian Licence for Open Government Data", result.getLabel().get("en"));
    }

    @Test
    void test_if_linguistic_system_by_code_query_returns_null() {
        graphQlTester.documentName("open-license-by-code")
                .variable("code", "INVALID")
                .execute()
                .path("$['data']['openLicenseByCode']")
                .valueIsNull();
    }
}
