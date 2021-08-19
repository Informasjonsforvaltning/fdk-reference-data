package no.fdk.referencedata.eu.filetype.eu;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.EuDatasetFetcher;
import no.fdk.referencedata.eu.filetype.FileType;
import no.fdk.referencedata.eu.filetype.FileTypeHarvester;
import no.fdk.referencedata.vocabulary.EUNotationType;
import no.fdk.referencedata.vocabulary.EUVOC;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class EUFileTypeHarvester implements FileTypeHarvester {

    private static final EuDatasetFetcher euDatasetFetcher = new EuDatasetFetcher("file-type");

    public Flux<FileType> harvestFileTypes() {
        log.info("Starting harvest of EU file types");
        final org.springframework.core.io.Resource fileTypesRdfSource = getFileTypesSource();
        if(fileTypesRdfSource == null) {
            return Flux.error(new Exception("Unable to fetch file-types distribution"));
        }

        final AtomicInteger count = new AtomicInteger();

        return Mono.justOrEmpty(getModel(fileTypesRdfSource))
                .flatMapIterable(m -> m.listSubjectsWithProperty(RDF.type, EUVOC.FileType).toList())
                .filter(Resource::isURIResource)
                .map(this::mapFileType)
                .doOnNext(fileType -> count.getAndIncrement())
                .doFinally(signal -> log.info("Successfully harvested {} EU file types", count.get()));
    }

    public String getVersion() {
        try {
            return euDatasetFetcher.getVersion();
        } catch(Exception e) {
            log.error("Unable to fetch latest data-theme version", e);
            return "0";
        }
    }

    public org.springframework.core.io.Resource getFileTypesSource() {
        try {
            return euDatasetFetcher.fetchResource("skos_ap_act/filetypes-skos-ap-act.rdf");
        } catch(Exception e) {
            log.error("Unable to retrieve filetypes source", e);
        }
        return null;
    }

    private Optional<Model> getModel(org.springframework.core.io.Resource resource) {
        try {
            return Optional.of(RDFDataMgr.loadModel(resource.getURI().toString(), Lang.RDFXML));
        } catch (IOException e) {
            log.error("Unable to load model", e);
            return Optional.empty();
        }
    }

    private FileType mapFileType(Resource fileType) {
        final StringBuilder ianaMediaType = new StringBuilder();

        Flux.fromIterable(fileType.listProperties(EUVOC.xlNotation).toList())
            .map(stmt -> stmt.getObject().asResource())
            .filter(resource -> resource.hasProperty(DCTerms.type, EUNotationType.IanaMT))
            .map(resource -> resource.getProperty(EUVOC.xlCodification).getString())
            .take(1)
            .doOnNext(ianaMediaType::append)
            .subscribe();

        return FileType.builder()
                .uri(fileType.getURI())
                .code(fileType.getProperty(DC.identifier).getObject().toString())
                .mediaType(ianaMediaType.toString())
                .build();
    }
}
