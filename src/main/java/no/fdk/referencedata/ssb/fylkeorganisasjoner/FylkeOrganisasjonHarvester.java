package no.fdk.referencedata.ssb.fylkeorganisasjoner;

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
public class FylkeOrganisasjonHarvester {

    public Flux<FylkeOrganisasjon> harvest() {
        log.info("Starting harvest of fylkeskommunale organisasjoner from ssb");
        try {
            final RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            final String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            final String url = getApiUrl() + "/classifications/589/correspondsAt?targetClassificationId=104&date=" + today;
            final ResponseEntity<String> response
                    = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode root = mapper.readTree(response.getBody()).path("correspondenceItems");

            final List<FylkeOrganisasjon> fylkeskommuner = new ArrayList<>();
            if (root.isArray()) {
                for (JsonNode fylkeskommune : root) {
                    fylkeskommuner.add(
                            buildFylkeskommune(
                                    fylkeskommune.path("sourceCode").textValue(),
                                    fylkeskommune.path("sourceName").textValue(),
                                    fylkeskommune.path("targetName").textValue(),
                                    fylkeskommune.path("targetCode").textValue())
                    );
                }
            }

            return Flux.fromIterable(fylkeskommuner);
        } catch(Exception e) {
            log.error("Unable to harvest fylkeskommunale organisasjoner from ssb", e);
            return Flux.error(e);
        }
    }

    public String getApiUrl() {
        return "https://data.ssb.no/api/klass/v1";
    }

    private FylkeOrganisasjon buildFylkeskommune(String organisasjonsnummer,
                                                 String organisasjonsnavn,
                                                 String fylkesnavn,
                                                 String fylkesnummer) {
        return FylkeOrganisasjon.builder()
                .uri("https://data.brreg.no/enhetsregisteret/api/enheter/" + organisasjonsnummer)
                .organisasjonsnummer(organisasjonsnummer)
                .organisasjonsnavn(organisasjonsnavn)
                .fylkesnavn(fylkesnavn)
                .fylkesnummer(fylkesnummer)
                .build();
    }
}
