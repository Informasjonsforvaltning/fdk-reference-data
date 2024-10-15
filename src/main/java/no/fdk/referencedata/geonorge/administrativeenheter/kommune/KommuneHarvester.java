package no.fdk.referencedata.geonorge.administrativeenheter.kommune;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class KommuneHarvester {

    public Flux<Kommune> harvest() {
        log.info("Starting harvest of GeoNorge kommuner");
        try {
            final RestTemplate restTemplate = new RestTemplate();
            final ResponseEntity<String> response
                    = restTemplate.getForEntity(getApiUrl() + "/kommuner", String.class);

            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode root = mapper.readTree(response.getBody());

            final List<Kommune> kommuner = new ArrayList<>();
            if (root.isArray()) {
                for (JsonNode kommune : root) {
                    kommuner.add(
                            buildKommune(
                                    kommune.path("kommunenavn").textValue(),
                                    kommune.path("kommunenavnNorsk").textValue(),
                                    kommune.path("kommunenummer").textValue())
                    );
                }
            }

            return Flux.fromIterable(kommuner);
        } catch(Exception e) {
            log.error("Unable to harvest GeoNorge kommuner", e);
            return Flux.error(e);
        }
    }

    public String getApiUrl() {
        return "https://ws.geonorge.no/kommuneinfo/v1";
    }

    private Kommune buildKommune(String kommunenavn, String kommunenavnNorsk, String kommunenummer) {
        return Kommune.builder()
                .uri("https://data.geonorge.no/administrativeEnheter/kommune/id/" + kommunenummer)
                .kommunenavn(kommunenavn)
                .kommunenavnNorsk(kommunenavnNorsk)
                .kommunenummer(kommunenummer)
                .build();
    }

}
