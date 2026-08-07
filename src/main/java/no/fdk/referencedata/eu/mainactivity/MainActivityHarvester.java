package no.fdk.referencedata.eu.mainactivity;

import no.fdk.referencedata.rdf.SkosMapper;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.eu.vocabulary.EUMainActivity;
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

@Component
@Slf4j
public class MainActivityHarvester extends AbstractEuHarvester<MainActivity> {


    public MainActivityHarvester() {
        super();
    }


    public Flux<MainActivity> harvest() {
        log.info("Starting harvest of EU main-activity");
        final org.springframework.core.io.Resource rdfSource = getSource();
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch main-activity distribution"));
        }

        return Mono.justOrEmpty(loadModel(rdfSource, false))
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        EUMainActivity.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapMainActivity);
    }


    private MainActivity mapMainActivity(Resource mainActivity) {
        Map<String, String> label = SkosMapper.extractLabels(mainActivity);

        return MainActivity.builder()
                .uri(mainActivity.getURI())
                .code(mainActivity.getProperty(DC.identifier).getObject().toString())
                .label(label)
                .build();
    }

    public String sparqlQuery() {
        return URLEncoder.encode(
                genericSPARQLQuery("main-activity"),
                StandardCharsets.UTF_8
        );
    }
}
