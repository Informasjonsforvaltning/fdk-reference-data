package no.fdk.referencedata.iana.mediatype;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
import java.util.function.Predicate;

import static java.lang.String.format;

@Component
@Slf4j
public class MediaTypeHarvester implements IanaHarvester {

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

    public Flux<MediaType> harvest() {
        log.info("Starting harvest of IANA media types");
        return getSources()
            .flatMap(s -> harvestMediaTypeResource(s));
    }

    public Flux<IanaSource> getSources()  {
        return registries
                .map(s -> {
                    String url = format("%s/%s.csv", BASE_URI, s);
                    try {
                        return new IanaSource(s, new UrlResource(url));
                    } catch (MalformedURLException e) {
                        log.error("Invalid IANA media type registry URL: {}", url, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull);
    }

    private Flux<MediaType> harvestMediaTypeResource(IanaSource ianaSource) {
        return Mono.justOrEmpty(ianaSource)
            .flatMapMany(r -> extractMediaTypeRegistryPairs(r))
            .map(pair -> buildMediaType(pair.getFirst(), pair.getSecond()));
    }

    private Flux<Pair<String, String>> extractMediaTypeRegistryPairs(IanaSource ianaSource) {
        try {
            CSVFormat format = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withSkipHeaderRecord();
            Charset charset = Charset.defaultCharset();

            List<CSVRecord> records = CSVParser.parse(ianaSource.getResource().getInputStream(),
                    charset, format).getRecords();

            return Flux.fromIterable(records)
                .map(record -> Pair.of(record.get(0).trim(), record.get(1).isBlank() ?
                        format("%s/%s", ianaSource.getType(), record.get(0).trim()) :
                        record.get(1).trim()))
                .filter(Predicate.not(pair -> pair.getFirst().isEmpty() || pair.getSecond().isEmpty()));
        } catch (IOException e) {
            log.error("Failed to extract media type registry records from: {}", ianaSource.getResource().getFilename(), e);
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
