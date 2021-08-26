package no.fdk.referencedata.iana.mediatype;

import reactor.core.publisher.Flux;

public interface IanaHarvester {
    Flux<IanaSource> getSources();
    Flux<MediaType> harvest();
}
