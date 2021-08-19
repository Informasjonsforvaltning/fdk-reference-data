package no.fdk.referencedata.eu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class EuDatasetFetcher {
    private static final String SEARCH_API = "https://data.europa.eu/api/hub/search";

    private final String dataset;

    public EuDatasetFetcher(String dataset) {
        this.dataset = dataset;
    }

    public String getVersion() throws Exception {
        final RestTemplate restTemplate = new RestTemplate();
        final ResponseEntity<String> response
                = restTemplate.getForEntity(SEARCH_API + "/datasets/" + dataset, String.class);

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode root = mapper.readTree(response.getBody());
        return root.path("result").path("version_info").textValue();
    }

    public Resource fetchResource(final String filePath) throws Exception {
        final RestTemplate restTemplate = new RestTemplate();
        final ResponseEntity<String> response
                = restTemplate.getForEntity(SEARCH_API + "/datasets/" + dataset, String.class);

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode root = mapper.readTree(response.getBody());

        final JsonNode distributions = root.path("result").path("distributions");
        if (distributions.isArray()) {
            for (JsonNode dist : distributions) {
                if (dist.path("access_url").textValue().contains(filePath)) {
                    return new UrlResource(dist.path("access_url").textValue());
                }
            }
        }

        return null;
    }
}
