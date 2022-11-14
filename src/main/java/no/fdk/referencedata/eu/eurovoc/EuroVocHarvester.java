package no.fdk.referencedata.eu.eurovoc;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.util.ZipUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.core.io.FileUrlResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class EuroVocHarvester extends AbstractEuHarvester<EuroVoc> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());
    private static final String cellarURI = "http://publications.europa.eu/resource/cellar/65e724e6-fe92-11ec-b94a-01aa75ed71a1.0001.04/DOC_1";
    private static final String rdfFileName = "eurovoc_in_skos_core_concepts.zip";
    private static final String VERSION = "0";

    public EuroVocHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<EuroVoc> harvest() {
        log.info("Starting harvest of EU eurovoc");
        final org.springframework.core.io.Resource source = getSource(cellarURI, rdfFileName);
        if(source == null) {
            return Flux.error(new Exception("Unable to fetch eurovoc distribution"));
        }

        try {
            final File destDir = Files.createTempDirectory("").toFile();
            ZipUtils.extractZip(source.getInputStream(), destDir);
            final org.springframework.core.io.Resource rdf =
                    new FileUrlResource(destDir.getAbsolutePath() + "/eurovoc_in_skos_core_concepts.rdf");

            return Mono.justOrEmpty(getModel(rdf))
                    .flatMapIterable(m -> m.listSubjectsWithProperty(RDF.type, SKOS.Concept).toList())
                    .filter(Resource::isURIResource)
                    .map(this::mapEuroVoc);

        } catch(IOException ex) {
            return Flux.error(ex);
        }
    }

    private EuroVoc mapEuroVoc(Resource euroVoc) {
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(euroVoc.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

       return EuroVoc.builder()
                .uri(euroVoc.getURI())
                .code(euroVoc.getURI().substring(euroVoc.getURI().lastIndexOf("/") + 1))
                .label(label)
                .build();
    }
}
