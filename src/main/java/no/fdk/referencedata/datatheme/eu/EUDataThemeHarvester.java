package no.fdk.referencedata.datatheme.eu;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.datatheme.DataTheme;
import no.fdk.referencedata.datatheme.DataThemeHarvester;
import no.fdk.referencedata.vocabulary.EUDataTheme;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class EUDataThemeHarvester implements DataThemeHarvester {

    private static final String SEARCH_API = "https://data.europa.eu/api/hub/search";

    private static final String[] SUPPORTED_LANGUAGES = new String[]{"en", "no", "nb", "nn"};

    public Flux<DataTheme> harvestDataThemes() {
        log.info("Starting harvest of EU data themes");
        final org.springframework.core.io.Resource dataThemesRdfSource = getDataThemesSource();
        if(dataThemesRdfSource == null) {
            return Flux.error(new Exception("Unable to fetch data-theme distribution"));
        }

        final AtomicInteger count = new AtomicInteger();

        return Mono.justOrEmpty(getModel(dataThemesRdfSource))
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        EUDataTheme.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapDataTheme)
                .doOnNext(fileType -> count.getAndIncrement())
                .doFinally(signal -> log.info("Successfully harvested {} EU data themes", count.get()));
    }

    public String getVersion() throws Exception {
        try {
            final RestTemplate restTemplate = new RestTemplate();
            final ResponseEntity<String> response
                    = restTemplate.getForEntity(SEARCH_API + "/datasets/data-theme", String.class);

            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode root = mapper.readTree(response.getBody());
            return root.path("result").path("version_info").textValue();
        } catch(JsonProcessingException e) {
            log.error("Could not parse JSON response", e);
            throw new Exception("Unable to fetch version");
        }
    }

    public org.springframework.core.io.Resource getDataThemesSource() {
        try {
            final RestTemplate restTemplate = new RestTemplate();
            final ResponseEntity<String> response
                    = restTemplate.getForEntity(SEARCH_API + "/datasets/data-theme", String.class);

            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode root = mapper.readTree(response.getBody());

            final JsonNode distributions = root.path("result").path("distributions");
            if (distributions.isArray()) {
                for (JsonNode dist : distributions) {
                    if (dist.path("title").path("en").textValue().equals("Data theme SKOS AP distribution")) {
                        return new UrlResource(dist.path("access_url").textValue());
                    }
                }
            }
        } catch(JsonProcessingException | MalformedURLException e) {
            log.error("Could not parse JSON response", e);
        }

        return null;
    }

    private Optional<Model> getModel(org.springframework.core.io.Resource resource) {
        try {
            return Optional.of(RDFDataMgr.loadModel(resource.getURI().toString(), Lang.RDFXML));
        } catch (IOException e) {
            log.error("Unable to load model", e);
            return Optional.empty();
        }
    }

    private DataTheme mapDataTheme(Resource dataTheme) {
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(dataTheme.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> Arrays.asList(SUPPORTED_LANGUAGES).contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

        return DataTheme.builder()
                .uri(dataTheme.getURI())
                .code(dataTheme.getProperty(DC.identifier).getObject().toString())
                .label(label)
                .build();
    }
}