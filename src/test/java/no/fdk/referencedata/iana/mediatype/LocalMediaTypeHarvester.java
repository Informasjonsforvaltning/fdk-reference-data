package no.fdk.referencedata.iana.mediatype;

import org.springframework.core.io.ClassPathResource;
import reactor.core.publisher.Flux;

public class LocalMediaTypeHarvester extends MediaTypeHarvester {
    @Override
    public Flux<IanaSource> getSources() {
        return Flux.just(new IanaSource("application", new ClassPathResource("mediatypes-test.csv")));
    }
}
