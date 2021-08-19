package no.fdk.referencedata.iana.mediatype;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;

public class LocalMediaTypeHarvester extends MediaTypeHarvester {
    @Override
    public Flux<Resource> getSources() {
        return Flux.just(new ClassPathResource("mediatypes-test.csv"));
    }
}
