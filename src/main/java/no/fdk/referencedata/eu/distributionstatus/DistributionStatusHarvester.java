package no.fdk.referencedata.eu.distributionstatus;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.eu.vocabulary.EUAuthorityOntology;
import no.fdk.referencedata.eu.vocabulary.EUDistributionStatus;
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
import java.util.stream.Collectors;

@Component
@Slf4j
public class DistributionStatusHarvester extends AbstractEuHarvester<DistributionStatus> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());
    private static String VERSION = "0";

    public DistributionStatusHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<DistributionStatus> harvest() {
        log.info("Starting harvest of EU distribution statuses");
        final org.springframework.core.io.Resource dataThemesRdfSource = getSource();
        if(dataThemesRdfSource == null) {
            return Flux.error(new Exception("Unable to fetch distribution statuses"));
        }

        return Mono.justOrEmpty(loadModel(dataThemesRdfSource, false))
                .doOnSuccess(this::updateVersion)
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme, EUDistributionStatus.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapDistributionStatus);
    }

    private void updateVersion(Model m) {
        VERSION = m.getProperty(
                m.getResource("http://publications.europa.eu/resource/authority/distribution-status"),
                OWL.versionInfo
        ).getString();
    }

    private DistributionStatus mapDistributionStatus(Resource distributionStatus) {
        return DistributionStatus.builder()
                .uri(distributionStatus.getURI())
                .code(distributionStatus.getProperty(DC.identifier).getObject().toString())
                .label(distributionStatus.listProperties(SKOS.prefLabel).toList().stream()
                        .map(stmt -> stmt.getObject().asLiteral())
                        .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                        .collect(Collectors.toMap(Literal::getLanguage, Literal::getString)))
                .startUse(distributionStatus.hasProperty(EUAuthorityOntology.startUse) ?
                        LocalDate.parse(distributionStatus.getProperty(EUAuthorityOntology.startUse).getString()) : null)
                .build();
    }

    public String sparqlQuery() {
        return URLEncoder.encode(
                genericSPARQLQuery("distribution-status"),
                StandardCharsets.UTF_8
        );
    }
}
