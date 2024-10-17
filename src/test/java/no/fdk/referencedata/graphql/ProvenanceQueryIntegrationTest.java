package no.fdk.referencedata.graphql;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.provenancestatement.ProvenanceStatement;
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
class ProvenanceQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void test_if_provenance_query_returns_valid_response() {
        List<ProvenanceStatement> result = graphQlTester.documentName("provenance-statements")
                .execute()
                .path("$['data']['provenanceStatements']")
                .entityList(ProvenanceStatement.class)
                .get();

        assertEquals(4, result.size());

        ProvenanceStatement provenanceStatement = result.get(0);

        assertEquals("http://data.brreg.no/datakatalog/provinens/bruker", provenanceStatement.getUri());
        assertEquals("BRUKER", provenanceStatement.getCode());
        assertEquals("Brukerinnsamlede data", provenanceStatement.getLabel().get("nb"));
        assertEquals("Brukerinnsamlede data", provenanceStatement.getLabel().get("nn"));
        assertEquals("User collection", provenanceStatement.getLabel().get("en"));
    }

    @Test
    void test_if_provenance_by_code_query_returns_valid_response() {
        ProvenanceStatement result = graphQlTester.documentName("provenance-statement-by-code")
                .variable("code", "NASJONAL")
                .execute()
                .path("$['data']['provenanceStatementByCode']")
                .entity(ProvenanceStatement.class)
                .get();

        assertEquals("http://data.brreg.no/datakatalog/provinens/nasjonal", result.getUri());
        assertEquals("NASJONAL", result.getCode());
        assertEquals("Autoritativ kilde", result.getLabel().get("nb"));
        assertEquals("Autoritativ kilde", result.getLabel().get("nn"));
        assertEquals("Authoritativ source", result.getLabel().get("en"));
    }

    @Test
    void test_if_provenance_by_code_query_returns_null() {
        graphQlTester.documentName("provenance-statement-by-code")
                .variable("code", "INVALID")
                .execute()
                .path("$['data']['provenanceStatementByCode']")
                .valueIsNull();
    }
}
