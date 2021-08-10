package no.fdk.referencedata.mediatype.iana;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.mediatype.MediaType;
import no.fdk.referencedata.mediatype.MediaTypeHarvester;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static java.lang.String.format;

@Component
@Slf4j
public class IANAMediaTypeHarvester implements MediaTypeHarvester {

    private static final String BASE_URI = "https://www.iana.org/assignments/media-types";

    private final Flux<String> registries = Flux.just(
        "application",
        "audio",
        "font",
        "image",
        "message",
        "model",
        "multipart",
        "text",
        "video"
    );

    public Flux<MediaType> harvestMediaTypes() {
        log.info("Starting harvest of IANA media types");

        AtomicInteger count = new AtomicInteger();

        return getMediaTypesSources()
            .flatMap(this::harvestMediaTypeResource)
            .doOnNext(MediaType -> count.getAndIncrement())
            .doFinally(signal -> log.info("Successfully harvested {} IANA media types", count.get()));
    }

    public Flux<Resource> getMediaTypesSources()  {
        return registries
                .map(s -> {
                    String url = format("%s/%s.csv", BASE_URI, s);
                    try {
                        return new UrlResource(url);
                    } catch (MalformedURLException e) {
                        log.error("Invalid IANA media type registry URL: {}", url, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .cast(Resource.class);
    }

    private Flux<MediaType> harvestMediaTypeResource(Resource resource) {
        return Mono.justOrEmpty(resource)
            .flatMapMany(this::extractMediaTypeRegistryPairs)
            .map(pair -> buildMediaType(pair.getFirst(), pair.getSecond()));
    }

    private Flux<Pair<String, String>> extractMediaTypeRegistryPairs(Resource resource) {
        try {
            CSVFormat format = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withSkipHeaderRecord();
            Charset charset = Charset.defaultCharset();

            List<CSVRecord> records = CSVParser.parse(resource.getInputStream(),
                    charset, format).getRecords();

            return Flux.fromIterable(records)
                .map(record -> Pair.of(record.get(0).trim(), record.get(1).trim()))
                .filter(Predicate.not(pair -> pair.getFirst().isEmpty() || pair.getSecond().isEmpty()));
        } catch (IOException e) {
            log.error("Failed to extract media type registry records from: {}", resource.getFilename(), e);
            return Flux.error(e);
        }
    }

    private MediaType buildMediaType(String name, String mediaType) {
        final String[] splitMediaType = mediaType.split("/");
        return MediaType
            .builder()
            .uri(format("%s/%s", BASE_URI, mediaType))
            .name(name)
            .type(splitMediaType[0])
            .subType(splitMediaType[1])
            .build();
    }

}
