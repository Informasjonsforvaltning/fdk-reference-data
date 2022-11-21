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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class FileTypeHarvester extends AbstractEuHarvester<FileType> {
    private static String VERSION = "0";

    public FileTypeHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<FileType> harvest() {
        log.info("Starting harvest of EU file types");
        final org.springframework.core.io.Resource fileTypesRdfSource = getSource(sparqlQuery());
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

    private String sparqlQuery() {
        String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
            "PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
            "PREFIX dct: <http://purl.org/dc/terms/> " +
            "PREFIX atres: <http://publications.europa.eu/resource/authority/> " +
            "PREFIX euvoc: <http://publications.europa.eu/ontology/euvoc#> " +
            "CONSTRUCT { " +
                "atres:file-type owl:versionInfo ?version . " +
                "?fileType a euvoc:FileType . " +
                "?fileType dc:identifier ?code . " +
                "?fileType euvoc:xlNotation ?xlNotation . " +
                "?xlNotation dct:type <http://publications.europa.eu/resource/authority/notation-type/IANA_MT> . " +
                "?xlNotation euvoc:xlCodification ?xlCodification . " +
            "} WHERE { " +
                "atres:file-type owl:versionInfo ?version . " +
                "?fileType skos:inScheme atres:file-type . " +
                "?fileType a euvoc:FileType . " +
                "?fileType dc:identifier ?code . " +
                "?fileType euvoc:xlNotation ?xlNotation . " +
                "?xlNotation dct:type <http://publications.europa.eu/resource/authority/notation-type/IANA_MT> . " +
                "?xlNotation euvoc:xlCodification ?xlCodification . " +
            "}";
        return URLEncoder.encode(query, StandardCharsets.UTF_8);
    }
}
