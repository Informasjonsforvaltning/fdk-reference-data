package no.fdk.referencedata.mediatype;

import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;

public interface MediaTypeHarvester {
    Flux<Resource> getMediaTypesSources();
    Flux<MediaType> harvestMediaTypes();
}
