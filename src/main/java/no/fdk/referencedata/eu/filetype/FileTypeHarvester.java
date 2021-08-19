package no.fdk.referencedata.eu.filetype;

import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;

public interface FileTypeHarvester {
    Resource getFileTypesSource();
    String getVersion() throws Exception;
    Flux<FileType> harvestFileTypes();
}
