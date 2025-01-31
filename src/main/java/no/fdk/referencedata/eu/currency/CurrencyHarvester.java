package no.fdk.referencedata.eu.currency;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.eu.vocabulary.EUAuthorityOntology;
import no.fdk.referencedata.eu.vocabulary.EUCurrency;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Literal;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static no.fdk.referencedata.i18n.Language.NORWEGIAN_BOKMAAL;
import static no.fdk.referencedata.i18n.Language.NORWEGIAN_NYNORSK;

@Component
@Slf4j
public class CurrencyHarvester extends AbstractEuHarvester<Currency> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());
    private static String VERSION = "0";

    public CurrencyHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<Currency> harvest() {
        log.info("Starting harvest of EU currencies");
        final org.springframework.core.io.Resource rdfSource = getSource();
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch EU currencies"));
        }

        return Mono.justOrEmpty(loadModel(rdfSource, false))
                .doOnSuccess(this::updateVersion)
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme, EUCurrency.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapCurrency);
    }

    private void updateVersion(Model m) {
        VERSION = m.getProperty(
                m.getResource("http://publications.europa.eu/resource/authority/currency"),
                OWL.versionInfo
        ).getString();
    }

    private Currency mapCurrency(Resource currency) {
        String code = currency.getProperty(DC.identifier).getObject().toString();
        Map<String, String> label = currency.listProperties(SKOS.prefLabel).toList().stream()
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .collect(Collectors.toMap(Literal::getLanguage, Literal::getString));

        switch (code) {
            case "NOK":
                label.put(NORWEGIAN_BOKMAAL.code(), "Norsk krone");
                label.put(NORWEGIAN_NYNORSK.code(), "Norsk krone");
                break;
            case "EUR":
                label.put(NORWEGIAN_BOKMAAL.code(), "Euro");
                label.put(NORWEGIAN_NYNORSK.code(), "Euro");
                break;
            case "GBP":
                label.put(NORWEGIAN_BOKMAAL.code(), "Britisk pund");
                label.put(NORWEGIAN_NYNORSK.code(), "Britisk pund");
                break;
            case "USD":
                label.put(NORWEGIAN_BOKMAAL.code(), "Amerikansk dollar");
                label.put(NORWEGIAN_NYNORSK.code(), "Amerikansk dollar");
                break;
            case "DKK":
                label.put(NORWEGIAN_BOKMAAL.code(), "Dansk krone");
                label.put(NORWEGIAN_NYNORSK.code(), "Dansk krone");
                break;
            case "SEK":
                label.put(NORWEGIAN_BOKMAAL.code(), "Svensk krone");
                label.put(NORWEGIAN_NYNORSK.code(), "Svensk krone");
                break;
            case "ISK":
                label.put(NORWEGIAN_BOKMAAL.code(), "Islandsk krone");
                label.put(NORWEGIAN_NYNORSK.code(), "Islandsk krone");
                break;
            case "JPY":
                label.put(NORWEGIAN_BOKMAAL.code(), "Japansk yen");
                label.put(NORWEGIAN_NYNORSK.code(), "Japansk yen");
        }

        return Currency.builder()
                .uri(currency.getURI())
                .code(code)
                .label(label)
                .startUse(currency.hasProperty(EUAuthorityOntology.startUse) ?
                        LocalDate.parse(currency.getProperty(EUAuthorityOntology.startUse).getString()) : null)
                .build();
    }

    public String sparqlQuery() {
        return URLEncoder.encode(
                genericSPARQLQuery("currency"),
                StandardCharsets.UTF_8
        );
    }
}
