package no.fdk.referencedata.eu.accessright;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.eu.vocabulary.EUAccessRight;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AccessRightHarvester extends AbstractEuHarvester<AccessRight> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());

    public AccessRightHarvester() {
        super("access-right", "skos_ap_act/access-right-skos-ap-act.rdf");
    }

    public Flux<AccessRight> harvest() {
        log.info("Starting harvest of EU access-rights");
        final org.springframework.core.io.Resource rdfSource = getSource();
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch access-right distribution"));
        }

        return Mono.justOrEmpty(getModel(rdfSource))
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        EUAccessRight.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapAccessRight);
    }

    private AccessRight mapAccessRight(Resource accessRight) {
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(accessRight.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

        return AccessRight.builder()
                .uri(accessRight.getURI())
                .code(accessRight.getProperty(DC.identifier).getObject().toString())
                .label(label)
                .build();
    }
}
