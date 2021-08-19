package no.fdk.referencedata.eu.eurovoc;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.eu.EuDatasetFetcher;
import no.fdk.referencedata.eu.eurovoc.Eurovoc;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.zip.ZipUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.core.io.FileUrlResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@Slf4j
public class EurovocHarvester extends AbstractEuHarvester<Eurovoc> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());

    public EurovocHarvester() {
        super("eurovoc", "skos_core/eurovoc_in_skos_core_concepts.zip");
    }

    public Flux<Eurovoc> harvest() {
        log.info("Starting harvest of EU data themes");
        final org.springframework.core.io.Resource source = getSource();
        if(source == null) {
            return Flux.error(new Exception("Unable to fetch eurovoc distribution"));
        }

        final AtomicInteger count = new AtomicInteger();

        try {
            final File destDir = Files.createTempDirectory("").toFile();
            ZipUtils.extractZip(source.getFile(), destDir);
            final org.springframework.core.io.Resource rdf =
                    new FileUrlResource(destDir.getAbsolutePath() + "/eurovoc_in_skos_core_concepts.rdf");

            return Mono.justOrEmpty(getModel(rdf))
                    .flatMapIterable(m -> m.listSubjectsWithProperty(RDF.type,
                            SKOS.Concept).toList())
                    .filter(Resource::isURIResource)
                    .map(this::mapEurovoc)
                    .doOnNext(fileType -> count.getAndIncrement())
                    .doFinally(signal -> log.info("Harvested {} EU eurovoc", count.get()));
        } catch(IOException ex) {
            return Flux.error(ex);
        }
    }

    private Eurovoc mapEurovoc(Resource eurovoc) {
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(eurovoc.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

       return Eurovoc.builder()
                .uri(eurovoc.getURI())
                .code(eurovoc.getURI().substring(eurovoc.getURI().lastIndexOf("/") + 1))
                .label(label)
                .build();
    }
}
