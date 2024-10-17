package no.fdk.referencedata.graphql;

import no.fdk.referencedata.adms.publishertype.PublisherType;
import no.fdk.referencedata.container.AbstractContainerTest;
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
class PublisherTypeQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void test_if_publisher_type_query_returns_valid_response() {
        List<PublisherType> result = graphQlTester.documentName("publisher-types")
                .execute()
                .path("$['data']['publisherTypes']")
                .entityList(PublisherType.class)
                .get();

        assertEquals(11, result.size());

        PublisherType publisherType = result.get(1);

        assertEquals("http://purl.org/adms/publishertype/Company", publisherType.getUri());
        assertEquals("Company", publisherType.getCode());
        assertEquals("Virksomhet", publisherType.getLabel().get("nb"));
        assertEquals("Verksemd", publisherType.getLabel().get("nn"));
        assertEquals("Company", publisherType.getLabel().get("en"));
    }

    @Test
    void test_if_publisher_type_by_code_query_returns_valid_response() {
        PublisherType result = graphQlTester.documentName("publisher-type-by-code")
                .variable("code", "SupraNationalAuthority")
                .execute()
                .path("$['data']['publisherTypeByCode']")
                .entity(PublisherType.class)
                .get();

        assertEquals("http://purl.org/adms/publishertype/SupraNationalAuthority", result.getUri());
        assertEquals("SupraNationalAuthority", result.getCode());
        assertEquals("Overnasjonal myndighet", result.getLabel().get("nb"));
        assertEquals("Overnasjonal myndigheit", result.getLabel().get("nn"));
        assertEquals("Supra-national authority", result.getLabel().get("en"));
    }

    @Test
    void test_if_publisher_type_by_code_query_returns_null() {
        graphQlTester.documentName("publisher-type-by-code")
                .variable("code", "INVALID")
                .execute()
                .path("$['data']['publisherTypeByCode']")
                .valueIsNull();
    }
}
