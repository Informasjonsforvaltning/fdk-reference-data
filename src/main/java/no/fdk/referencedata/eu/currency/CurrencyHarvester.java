package no.fdk.referencedata.eu.currency;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.eu.vocabulary.EUAuthorityOntology;
import no.fdk.referencedata.eu.vocabulary.EUCurrency;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CurrencyHarvester extends AbstractEuHarvester<Currency> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());

    private final Map<String, Map<String, String>> missingTranslations = Map.ofEntries(
            Map.entry(EUCurrency.getURI() + "/NOK", Map.of(
                    "nb", "Norsk krone",
                    "nn", "Norsk krone"
            )),
            Map.entry(EUCurrency.getURI() + "/EUR", Map.of(
                    "nb", "Euro",
                    "nn", "Euro"
            )),
            Map.entry(EUCurrency.getURI() + "/GBP", Map.of(
                    "nb", "Britisk pund",
                    "nn", "Britisk pund"
            )),
            Map.entry(EUCurrency.getURI() + "/USD", Map.of(
                    "nb", "Amerikansk dollar",
                    "nn", "Amerikansk dollar"
            )),
            Map.entry(EUCurrency.getURI() + "/DKK", Map.of(
                    "nb", "Dansk krone",
                    "nn", "Dansk krone"
            )),
            Map.entry(EUCurrency.getURI() + "/SEK", Map.of(
                    "nb", "Svensk krone",
                    "nn", "Svensk krone"
            )),
            Map.entry(EUCurrency.getURI() + "/ISK", Map.of(
                    "nb", "Islandsk krone",
                    "nn", "Islandsk krone"
            )),
            Map.entry(EUCurrency.getURI() + "/JPY", Map.of(
                    "nb", "Japansk yen",
                    "nn", "Japansk yen"
            ))
    );

    public CurrencyHarvester() {
        super();
    }

    public Model translateCurrencies(Model model) {
        Model translated = ModelFactory.createDefaultModel();
        model.listStatements().forEach(translated::add);

        for (String subject : missingTranslations.keySet()) {
            Resource subjectResource = model.getResource(subject);
            Map<String, String> subjectTranslations = missingTranslations.get(subject);
            for (Map.Entry<String, String> entry : subjectTranslations.entrySet()) {
                translated.add(
                        subjectResource,
                        SKOS.prefLabel,
                        entry.getValue(),
                        entry.getKey()
                );
            }
        }

        updateModel(translated);
        return translated;
    }

    private Optional<Model> loadAndTranslateModel(org.springframework.core.io.Resource rdfSource) {
        return loadModel(rdfSource, false)
                .map(this::translateCurrencies);
    }

    public Flux<Currency> harvest() {
        log.info("Starting harvest of EU currencies");
        final org.springframework.core.io.Resource rdfSource = getSource();
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch EU currencies"));
        }

        return Mono.justOrEmpty(loadAndTranslateModel(rdfSource))
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme, EUCurrency.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapCurrency);
    }

    private Currency mapCurrency(Resource currency) {
        return Currency.builder()
                .uri(currency.getURI())
                .code(currency.getProperty(DC.identifier).getObject().toString())
                .label(currency.listProperties(SKOS.prefLabel).toList().stream()
                        .map(stmt -> stmt.getObject().asLiteral())
                        .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                        .collect(Collectors.toMap(Literal::getLanguage, Literal::getString)))
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
