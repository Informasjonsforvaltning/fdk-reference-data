package no.fdk.referencedata;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.los.LosService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "server.servlet.context-path=/reference-data"
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class CompressionIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private LosService losService;

    // java.net.http never decompresses on its own, so Content-Encoding and the raw bytes are observable.
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    public void setup() {
        losService.importLosNodes();
    }

    private HttpResponse<byte[]> get(String path, String accept, String acceptEncoding) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/reference-data" + path)).GET();
        if (accept != null) {
            builder.header("Accept", accept);
        }
        if (acceptEncoding != null) {
            builder.header("Accept-Encoding", acceptEncoding);
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
    }

    private static void assertGzipped(HttpResponse<byte[]> response) {
        assertEquals(200, response.statusCode());
        assertEquals("gzip", response.headers().firstValue("Content-Encoding").orElse(null));
        assertTrue(String.join(",", response.headers().allValues("Vary")).toLowerCase().contains("accept-encoding"),
                "Vary must include Accept-Encoding");
        byte[] body = response.body();
        assertTrue(body.length > 2 && body[0] == (byte) 0x1f && body[1] == (byte) 0x8b, "body is not gzip");
    }

    @Test
    public void gzips_json_response_when_client_accepts_it() throws Exception {
        assertGzipped(get("/v3/api-docs", null, "gzip"));
    }

    @Test
    public void leaves_response_uncompressed_when_client_does_not_ask() throws Exception {
        HttpResponse<byte[]> response = get("/v3/api-docs", null, null);
        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Encoding").isEmpty());
        assertTrue(response.body().length > 1024, "test endpoint must exceed the 1 KB threshold");
    }

    @Test
    public void gzips_turtle_response_when_client_accepts_it() throws Exception {
        HttpResponse<byte[]> response = get("/los/themes-and-words", "text/turtle", "gzip");
        assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("text/turtle"),
                "expected a text/turtle response");
        assertGzipped(response);
    }
}
