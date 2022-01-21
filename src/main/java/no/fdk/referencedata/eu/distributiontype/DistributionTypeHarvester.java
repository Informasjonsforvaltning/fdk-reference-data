package no.fdk.referencedata.eu.distributiontype;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.eu.vocabulary.EUAuthorityOntology;
import no.fdk.referencedata.eu.vocabulary.EUDistributionType;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DistributionTypeHarvester extends AbstractEuHarvester<DistributionType> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());

    public DistributionTypeHarvester() {
        super("distribution-type", "skos_core/distribution-types-skos.rdf");
    }

    public Flux<DistributionType> harvest() {
        log.info("Starting harvest of EU distribution types");
        final org.springframework.core.io.Resource dataThemesRdfSource = getSource();
        if(dataThemesRdfSource == null) {
            return Flux.error(new Exception("Unable to fetch distribution-types distribution"));
        }

        return Mono.justOrEmpty(getModel(dataThemesRdfSource))
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme, EUDistributionType.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapDistributionType);
    }

    private DistributionType mapDistributionType(Resource distributionType) {
        return DistributionType.builder()
                .uri(distributionType.getURI())
                .code(distributionType.getProperty(DC.identifier).getObject().toString())
                .label(distributionType.listProperties(SKOS.prefLabel).toList().stream()
                        .map(stmt -> stmt.getObject().asLiteral())
                        .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                        .collect(Collectors.toMap(Literal::getLanguage, Literal::getString)))
                .startUse(distributionType.hasProperty(EUAuthorityOntology.startUse) ?
                        LocalDate.parse(distributionType.getProperty(EUAuthorityOntology.startUse).getString()) : null)
                .build();
    }
}
