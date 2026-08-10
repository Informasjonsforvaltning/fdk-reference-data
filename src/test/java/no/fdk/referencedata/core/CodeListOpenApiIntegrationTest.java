package no.fdk.referencedata.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.fdk.referencedata.container.AbstractContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "scheduling.enabled=false",
        })
@ActiveProfiles("test")
class CodeListOpenApiIntegrationTest extends AbstractContainerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void documentsRegistryDrivenCodeListRoutes() throws Exception {
        String body = restClient.get()
                .uri("/v3/api-docs")
                .retrieve()
                .body(String.class);
        JsonNode paths = objectMapper.readTree(body).path("paths");

        JsonNode frequencies = paths.path("/eu/frequencies");
        assertTrue(frequencies.has("get"));
        assertTrue(frequencies.has("post"));
        assertEquals("apiKey", frequencies.path("post").path("security").get(0).fieldNames().next());
        assertTrue(frequencies.path("get").path("responses").path("200").path("content").has("application/json"));
        assertTrue(frequencies.path("get").path("responses").path("200").path("content").has("text/turtle"));
        assertTrue(paths.has("/eu/frequencies/{code}"));

        JsonNode fylke = paths.path("/ssb/fylke-organisasjoner");
        assertTrue(fylke.has("get"));
        assertTrue(fylke.has("post"));
        assertFalse(fylke.path("get").path("responses").path("200").path("content").has("text/turtle"));
        assertTrue(paths.has("/ssb/fylke-organisasjoner/{fylkesnummer}"));

        JsonNode referenceTypes = paths.path("/reference-types");
        assertTrue(referenceTypes.has("get"));
        assertFalse(referenceTypes.has("post"));
        assertFalse(referenceTypes.path("get").path("responses").path("200").path("content").has("text/turtle"));
        assertTrue(paths.has("/reference-types/{code}"));

        assertNotNull(paths.path("/adms/statuses").path("get"));
        assertFalse(paths.path("/adms/statuses").has("post"));
    }
}
