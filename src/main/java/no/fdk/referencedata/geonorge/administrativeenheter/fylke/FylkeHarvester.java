package no.fdk.referencedata.geonorge.administrativeenheter.fylke;

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
public class FylkeHarvester {

    public Flux<Fylke> harvest() {
        log.info("Starting harvest of GeoNorge fylker");
        try {
            final RestTemplate restTemplate = new RestTemplate();
            final ResponseEntity<String> response
                    = restTemplate.getForEntity(getApiUrl() + "/fylker", String.class);

            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode root = mapper.readTree(response.getBody());

            final List<Fylke> fylker = new ArrayList<>();
            if (root.isArray()) {
                for (JsonNode fylke : root) {
                    fylker.add(
                            buildFylke(
                                    fylke.path("fylkesnavn").textValue(),
                                    fylke.path("fylkesnummer").textValue())
                    );
                }
            }

            return Flux.fromIterable(fylker);
        } catch(Exception e) {
            log.error("Unable to harvest GeoNorge fylker", e);
            return Flux.error(e);
        }
    }

    public String getApiUrl() {
        return "https://ws.geonorge.no/kommuneinfo/v1";
    }

    private Fylke buildFylke(String fylkesnavn, String fylkesnummer) {
        return Fylke.builder()
                .uri("https://data.geonorge.no/administrativeEnheter/fylke/id/" + fylkesnummer)
                .fylkesnavn(fylkesnavn)
                .fylkesnummer(fylkesnummer)
                .build();
    }

}
