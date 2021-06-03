package no.fdk.referencedata.filetype.eu;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.filetype.FileType;
import no.fdk.referencedata.filetype.FileTypeHarvester;
import no.fdk.referencedata.vocabulary.EUNotationType;
import no.fdk.referencedata.vocabulary.EUVOC;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class EUFileTypeHarvester implements FileTypeHarvester {

    private static final String SEARCH_API = "https://data.europa.eu/api/hub/search";

    public Flux<FileType> harvestFileTypes() {
        log.info("Starting harvest of EU file types");
        final org.springframework.core.io.Resource fileTypesRdfSource = getFileTypesSource();
        if(fileTypesRdfSource == null) {
            return Flux.error(new Exception("Unable to fetch file-types distribution"));
        }

        final AtomicInteger count = new AtomicInteger();

        return Mono.justOrEmpty(getModel(fileTypesRdfSource))
                .flatMapIterable(m -> m.listSubjectsWithProperty(RDF.type, EUVOC.FileType).toList())
                .filter(Resource::isURIResource)
                .map(this::mapFileType)
                .doOnNext(fileType -> count.getAndIncrement())
                .doFinally(signal -> log.info("Successfully harvested {} EU file types", count.get()));
    }

    public String getVersion() throws Exception {
        try {
            final RestTemplate restTemplate = new RestTemplate();
            final ResponseEntity<String> response
                    = restTemplate.getForEntity(SEARCH_API + "/datasets/file-type", String.class);

            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode root = mapper.readTree(response.getBody());
            return root.path("result").path("version_info").textValue();
        } catch(JsonProcessingException e) {
            log.error("Could not parse JSON response", e);
            throw new Exception("Unable to fetch version");
        }
    }

    public org.springframework.core.io.Resource getFileTypesSource() {
        try {
            final RestTemplate restTemplate = new RestTemplate();
            final ResponseEntity<String> response
                    = restTemplate.getForEntity(SEARCH_API + "/datasets/file-type", String.class);

            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode root = mapper.readTree(response.getBody());

            final JsonNode distributions = root.path("result").path("distributions");
            if (distributions.isArray()) {
                for (JsonNode dist : distributions) {
                    if (dist.path("title").path("en").textValue().equals("File type SKOS AP distribution")) {
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

    private FileType mapFileType(Resource fileType) {
        final StringBuilder ianaMediaType = new StringBuilder();

        Flux.fromIterable(fileType.listProperties(EUVOC.xlNotation).toList())
            .map(stmt -> stmt.getObject().asResource())
            .filter(resource -> resource.hasProperty(DCTerms.type, EUNotationType.IanaMT))
            .map(resource -> resource.getProperty(EUVOC.xlCodification).getString())
            .take(1)
            .doOnNext(ianaMediaType::append)
            .subscribe();

        return FileType.builder()
                .uri(fileType.getURI())
                .code(fileType.getProperty(DC.identifier).getObject().toString())
                .mediaType(ianaMediaType.toString())
                .build();
    }
}