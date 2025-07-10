package no.fdk.referencedata.eu.frequency;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.eu.vocabulary.EUFrequency;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static no.fdk.referencedata.i18n.Language.NORWEGIAN_BOKMAAL;
import static no.fdk.referencedata.i18n.Language.NORWEGIAN_NYNORSK;

@Component
@Slf4j
public class FrequencyHarvester extends AbstractEuHarvester<Frequency> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());
    private static String VERSION = "0";

    public FrequencyHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<Frequency> harvest() {
        log.info("Starting harvest of EU frequencies");
        final org.springframework.core.io.Resource rdfSource = getSource();
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch frequency distribution"));
        }

        return Mono.justOrEmpty(loadModel(rdfSource, false))
                .doOnSuccess(this::updateVersion)
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        EUFrequency.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapFrequency);
    }

    private void updateVersion(Model m) {
        VERSION = m.getProperty(
                m.getResource("http://publications.europa.eu/resource/authority/frequency"),
                OWL.versionInfo
        ).getString();
    }

    private Frequency mapFrequency(Resource frequency) {
        String code = frequency.getProperty(DC.identifier).getObject().toString();
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(frequency.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

        int sortIndex = switch (code) {
            case "CONT" -> 0;
            case "UPDATE_CONT" -> 1;
            case "1MIN" -> 2;
            case "5MIN" -> 3;
            case "10MIN" -> 4;
            case "15MIN" -> 5;
            case "30MIN" -> 6;
            case "HOURLY" -> 7;
            case "BIHOURLY" -> 8;
            case "TRIHOURLY" -> 9;
            case "12HRS" -> 10;
            case "DAILY_2" -> 11;
            case "DAILY" -> 12;
            case "WEEKLY_5" -> 13;
            case "WEEKLY_3" -> 14;
            case "WEEKLY_2" -> 15;
            case "WEEKLY" -> 16;
            case "BIWEEKLY" -> 17;
            case "MONTHLY_3" -> 18;
            case "MONTHLY_2" -> 19;
            case "MONTHLY" -> 20;
            case "BIMONTHLY" -> 21;
            case "QUARTERLY" -> 22;
            case "ANNUAL_3" -> 23;
            case "ANNUAL_2" -> 24;
            case "ANNUAL" -> 25;
            case "BIENNIAL" -> 26;
            case "TRIENNIAL" -> 27;
            case "QUADRENNIAL" -> 28;
            case "QUINQUENNIAL" -> 29;
            case "DECENNIAL" -> 30;
            case "BIDECENNIAL" -> 31;
            case "TRIDECENNIAL" -> 32;
            case "AS_NEEDED" -> 33;
            case "IRREG" -> 34;
            case "OTHER" -> 35;
            case "NOT_PLANNED" -> 36;
            case "NEVER" -> 37;
            default -> 100;
        };

        return Frequency.builder()
                .uri(frequency.getURI())
                .code(frequency.getProperty(DC.identifier).getObject().toString())
                .label(label)
                .sortIndex(sortIndex)
                .build();
    }

    public String sparqlQuery() {
        return URLEncoder.encode(
                genericSPARQLQuery("frequency"),
                StandardCharsets.UTF_8
        );
    }
}
