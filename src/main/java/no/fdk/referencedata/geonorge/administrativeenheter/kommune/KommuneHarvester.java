package no.fdk.referencedata.geonorge.administrativeenheter.kommune;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.Fylke;
import no.fdk.referencedata.iana.mediatype.IanaHarvester;
import no.fdk.referencedata.iana.mediatype.IanaSource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.UrlResource;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static java.lang.String.format;

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
