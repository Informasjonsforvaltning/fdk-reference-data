package no.fdk.referencedata.lovdata;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.core.HarvestParseException;
import no.fdk.referencedata.core.HarvestSourceException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class LovdataHarvester {

    private static final String BASE_URI = "https://lovdata.no";
    private static final int HEADER_BYTES = 9000;

    // A lower share means Lovdata changed their document structure
    private static final double MINIMUM_PARSED_SHARE = 0.95;

    private static final Map<String, String> SOURCES = new LinkedHashMap<>(Map.of(
            "lov", "https://api.lovdata.no/v1/publicData/get/gjeldende-lover.tar.bz2",
            "forskrift", "https://api.lovdata.no/v1/publicData/get/gjeldende-sentrale-forskrifter.tar.bz2"));

    private static final Pattern ELI_FIELD = fieldPattern("eli");
    private static final Pattern DOKID_FIELD = fieldPattern("dokid");
    private static final Pattern TITLE_FIELD = fieldPattern("title");
    private static final Pattern LEGACY_ID_FIELD = fieldPattern("legacyID");

    private static final Pattern DOKID_FORMAT =
            Pattern.compile("^\\w+/(\\w+)/(\\d{4})-(\\d{2})-(\\d{2})(?:-(\\d+))?$");
    private static final Pattern LANGUAGE = Pattern.compile("<html lang=\"(\\w+)\"");
    private static final Pattern DOCUMENT_TITLE = Pattern.compile("<title>(.*?)</title>", Pattern.DOTALL);
    private static final Pattern TAGS = Pattern.compile("<[^>]+>");
    private static final Pattern ENTITY = Pattern.compile("&(?:#(\\d+)|#[xX]([0-9a-fA-F]+)|(\\w+));");

    private static Pattern fieldPattern(String name) {
        return Pattern.compile("<dd class=\"" + name + "\">(.*?)</dd>", Pattern.DOTALL);
    }

    public Flux<LegalResource> harvest() {
        log.info("Starting harvest of legal resources from Lovdata");
        Map<String, LegalResource> byUri = new LinkedHashMap<>();
        for (Map.Entry<String, String> source : SOURCES.entrySet()) {
            try {
                collect(source.getValue(), source.getKey(), byUri);
            } catch (IOException e) {
                return Flux.error(new HarvestSourceException(
                        "Unable to download %s documents from %s".formatted(source.getKey(), source.getValue()), e));
            }
        }
        log.info("Harvested {} legal resources from Lovdata", byUri.size());
        return Flux.fromIterable(byUri.values());
    }

    private void collect(String url, String type, Map<String, LegalResource> byUri) throws IOException {
        int documents = 0;
        int parsed = 0;
        try (InputStream in = URI.create(url).toURL().openStream();
             TarArchiveInputStream tar =
                     new TarArchiveInputStream(new BZip2CompressorInputStream(new BufferedInputStream(in)))) {
            TarArchiveEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                if (!entry.isFile()) {
                    continue;
                }
                documents++;
                if (merge(byUri, read(tar), type)) {
                    parsed++;
                }
            }
        }
        verifyShareParsed(url, type, documents, parsed);
    }

    void verifyShareParsed(String url, String type, int documents, int parsed) {
        if (documents == 0) {
            throw new HarvestSourceException("No %s documents in %s".formatted(type, url));
        }
        double share = (double) parsed / documents;
        if (share < MINIMUM_PARSED_SHARE) {
            throw new HarvestParseException(
                    "Only %d of %d %s documents could be parsed, expected at least %.0f%%. Has the Lovdata document structure changed?"
                            .formatted(parsed, documents, type, MINIMUM_PARSED_SHARE * 100));
        }
        log.info("Parsed {} of {} {} documents", parsed, documents, type);
    }

    private String read(TarArchiveInputStream tar) throws IOException {
        byte[] buffer = new byte[HEADER_BYTES];
        int read = tar.readNBytes(buffer, 0, HEADER_BYTES);
        return new String(buffer, 0, read, StandardCharsets.UTF_8);
    }

    LegalResource parse(String header, String type) {
        String eli = match(ELI_FIELD, header);
        if (eli == null) {
            eli = deriveEli(match(DOKID_FIELD, header));
        }
        String title = match(TITLE_FIELD, header);
        if (title == null) {
            title = match(DOCUMENT_TITLE, header);
        }
        if (eli == null || title == null) {
            return null;
        }

        Map<String, String> label = new LinkedHashMap<>();
        label.put(language(header), title);
        return LegalResource.builder()
                .uri(BASE_URI + eli)
                .identifier(match(LEGACY_ID_FIELD, header))
                .label(label)
                .type(type)
                .build();
    }

    private boolean merge(Map<String, LegalResource> byUri, String header, String type) {
        LegalResource parsed = parse(header, type);
        if (parsed == null) {
            log.warn("Skipping Lovdata document without uri or title");
            return false;
        }
        LegalResource existing = byUri.get(parsed.getUri());
        if (existing == null) {
            byUri.put(parsed.getUri(), parsed);
        } else {
            parsed.getLabel().forEach(existing.getLabel()::putIfAbsent);
        }
        return true;
    }

    private String language(String header) {
        String language = match(LANGUAGE, header);
        if (language == null || "no".equals(language)) {
            return "nb";
        }
        return language;
    }

    private String deriveEli(String dokid) {
        if (dokid == null) {
            return null;
        }
        Matcher matcher = DOKID_FORMAT.matcher(dokid);
        if (!matcher.matches()) {
            return null;
        }
        String number = matcher.group(5) == null
                ? "0"
                : String.format("%02d", Integer.parseInt(matcher.group(5)));
        return "/eli/%s/%s/%s/%s/%s".formatted(
                matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), number);
    }

    private String match(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        String value = decodeEntities(TAGS.matcher(matcher.group(1)).replaceAll("")).trim();
        return value.isEmpty() ? null : value;
    }

    String decodeEntities(String text) {
        Matcher matcher = ENTITY.matcher(text);
        StringBuilder decoded = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(decoded, Matcher.quoteReplacement(replacement(matcher)));
        }
        matcher.appendTail(decoded);
        return decoded.toString();
    }

    private String replacement(Matcher matcher) {
        if (matcher.group(1) != null) {
            return character(Integer.parseInt(matcher.group(1)));
        }
        if (matcher.group(2) != null) {
            return character(Integer.parseInt(matcher.group(2), 16));
        }
        return switch (matcher.group(3)) {
            case "amp" -> "&";
            case "lt" -> "<";
            case "gt" -> ">";
            case "quot" -> "\"";
            case "apos" -> "'";
            case "nbsp" -> " ";
            default -> matcher.group();
        };
    }

    private String character(int codePoint) {
        if (!Character.isValidCodePoint(codePoint)) {
            return "";
        }
        return Character.isSpaceChar(codePoint) ? " " : Character.toString(codePoint);
    }
}
