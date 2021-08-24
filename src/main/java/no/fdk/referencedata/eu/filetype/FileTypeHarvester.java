package no.fdk.referencedata.eu.filetype;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.eu.vocabulary.EUNotationType;
import no.fdk.referencedata.eu.vocabulary.EUVOC;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class FileTypeHarvester extends AbstractEuHarvester<FileType> {

    public FileTypeHarvester() {
        super("file-type", "skos_ap_act/filetypes-skos-ap-act.rdf");
    }

    public Flux<FileType> harvest() {
        log.info("Starting harvest of EU file types");
        final org.springframework.core.io.Resource fileTypesRdfSource = getSource();
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
