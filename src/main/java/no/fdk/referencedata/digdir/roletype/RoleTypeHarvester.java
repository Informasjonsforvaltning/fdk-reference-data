package no.fdk.referencedata.digdir.roletype;

import no.fdk.referencedata.rdf.SkosMapper;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.digdir.AbstractDataNorgeHarvester;
import no.fdk.referencedata.digdir.vocabulary.RoleTypeVocabulary;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RoleTypeHarvester extends AbstractDataNorgeHarvester<RoleType> {

    private static final String PATH = "role-type";

    public RoleTypeHarvester() {
        super();
    }


    public Flux<RoleType> harvest() {
        log.info("Starting harvest of data.norge role-types");
        final org.springframework.core.io.Resource rdfSource = getSource(PATH);
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch role-type distribution"));
        }

        loadModel(rdfSource);

        return Mono.justOrEmpty(getModel())
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        RoleTypeVocabulary.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapRoleType);
    }


    private RoleType mapRoleType(Resource roleType) {
        Map<String, String> label = SkosMapper.extractLabels(roleType);

        return RoleType.builder()
                .uri(roleType.getURI())
                .code(roleType.getProperty(DCTerms.identifier).getString().split("#")[1])
                .label(label)
                .build();
    }
}
