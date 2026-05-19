package no.fdk.referencedata.geonames;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class GeonamesHarvester {

    private static final String NORWAY_GEONAME_ID = "3144096";
    private static final String GEONAMES_URI_BASE = "https://sws.geonames.org/";

    @Value("${application.geonames.user}")
    private String username;

    public Flux<GeonamesFylke> harvestFylker() {
        log.info("Starting harvest of Norwegian counties from GeoNames");
        try {
            JsonNode geonames = fetchChildren(NORWAY_GEONAME_ID);
            List<GeonamesFylke> fylker = new ArrayList<>();
            if (geonames.isArray()) {
                for (JsonNode node : geonames) {
                    String geonameId = node.path("geonameId").asText();
                    fylker.add(GeonamesFylke.builder()
                            .uri(GEONAMES_URI_BASE + geonameId + "/")
                            .geonameId(geonameId)
                            .name(node.path("name").asText())
                            .build());
                }
            }
            return Flux.fromIterable(fylker);
        } catch (Exception e) {
            log.error("Unable to harvest Norwegian counties from GeoNames", e);
            return Flux.error(e);
        }
    }

    public Flux<GeonamesKommune> harvestKommunerForFylke(String fylkeGeonameId) {
        log.debug("Harvesting districts for county geonameId={}", fylkeGeonameId);
        try {
            JsonNode geonames = fetchChildren(fylkeGeonameId);
            List<GeonamesKommune> kommuner = new ArrayList<>();
            if (geonames.isArray()) {
                for (JsonNode node : geonames) {
                    String geonameId = node.path("geonameId").asText();
                    kommuner.add(GeonamesKommune.builder()
                            .uri(GEONAMES_URI_BASE + geonameId + "/")
                            .geonameId(geonameId)
                            .name(node.path("name").asText())
                            .fylkeGeonameId(fylkeGeonameId)
                            .build());
                }
            }
            return Flux.fromIterable(kommuner);
        } catch (Exception e) {
            log.error("Unable to harvest districts for county geonameId={}", fylkeGeonameId, e);
            return Flux.error(e);
        }
    }

    private JsonNode fetchChildren(String geonameId) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        String url = getApiUrl() + "/childrenJSON?geonameId=" + geonameId + "&username=" + getUsername();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(response.getBody()).path("geonames");
    }

    public String getApiUrl() {
        return "http://api.geonames.org";
    }

    public String getUsername() {
        return username;
    }
}
