package no.fdk.referencedata.eu.filetype;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.eu.vocabulary.EUNotationType;
import no.fdk.referencedata.eu.vocabulary.EUVOC;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class FileTypeHarvester extends AbstractEuHarvester<FileType> {
    private static final String cellarURI = "http://publications.europa.eu/resource/cellar/bfc76ea5-3e74-11ed-92ed-01aa75ed71a1.0001.03/DOC_1";
    private static final String rdfFileName = "filetypes-skos-ap-act.rdf";
    private static String VERSION = "0";

    public FileTypeHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<FileType> harvest() {
        log.info("Starting harvest of EU file types");
        final org.springframework.core.io.Resource fileTypesRdfSource = getSource(cellarURI, rdfFileName);
        if(fileTypesRdfSource == null) {
            return Flux.error(new Exception("Unable to fetch file-types distribution"));
        }

        return Mono.justOrEmpty(getModel(fileTypesRdfSource))
                .doOnSuccess(this::updateVersion)
                .flatMapIterable(m -> m.listSubjectsWithProperty(RDF.type, EUVOC.FileType).toList())
                .filter(Resource::isURIResource)
                .map(this::mapFileType);
    }

    private void updateVersion(Model m) {
        VERSION = m.getProperty(
                m.getResource("http://publications.europa.eu/resource/authority/file-type"),
                OWL.versionInfo
        ).getString();
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
