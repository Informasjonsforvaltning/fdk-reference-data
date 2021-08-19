package no.fdk.referencedata.iana.mediatype;

import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;

public interface IanaHarvester {
    Flux<Resource> getSources();
    Flux<MediaType> harvest();
}
