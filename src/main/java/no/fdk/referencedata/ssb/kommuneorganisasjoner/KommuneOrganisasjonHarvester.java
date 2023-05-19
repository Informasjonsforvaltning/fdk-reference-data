package no.fdk.referencedata.ssb.kommuneorganisasjoner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class KommuneOrganisasjonHarvester {

    public Flux<KommuneOrganisasjon> harvest() {
        log.info("Starting harvest of kommunale organisasjoner from ssb");
        try {
            final RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            final String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            final String url = getApiUrl() + "/classifications/582/correspondsAt?targetClassificationId=131&date=" + today;
            final ResponseEntity<String> response
                    = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode root = mapper.readTree(response.getBody()).path("correspondenceItems");

            final List<KommuneOrganisasjon> kommuner = new ArrayList<>();
            if (root.isArray()) {
                for (JsonNode kommune : root) {
                    kommuner.add(
                            buildKommune(
                                    kommune.path("sourceCode").textValue(),
                                    kommune.path("sourceName").textValue(),
                                    kommune.path("targetName").textValue(),
                                    kommune.path("targetCode").textValue())
                    );
                }
            }

            return Flux.fromIterable(kommuner);
        } catch(Exception e) {
            log.error("Unable to harvest kommunale organisasjoner from ssb", e);
            return Flux.error(e);
        }
    }

    public String getApiUrl() {
        return "https://data.ssb.no/api/klass/v1";
    }

    private KommuneOrganisasjon buildKommune(String organisasjonsnummer,
                                                   String organisasjonsnavn,
                                                   String kommunenavn,
                                                   String kommunenummer) {
        return KommuneOrganisasjon.builder()
                .uri("https://data.brreg.no/enhetsregisteret/api/enheter/" + organisasjonsnummer)
                .organisasjonsnummer(organisasjonsnummer)
                .organisasjonsnavn(organisasjonsnavn)
                .kommunenavn(kommunenavn)
                .kommunenummer(kommunenummer)
                .build();
    }
}
