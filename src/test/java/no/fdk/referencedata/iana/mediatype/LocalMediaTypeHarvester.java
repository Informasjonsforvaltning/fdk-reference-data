package no.fdk.referencedata.iana.mediatype;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.util.Pair;
import reactor.core.publisher.Flux;

public class LocalMediaTypeHarvester extends MediaTypeHarvester {
    @Override
    public Flux<IanaSource> getSources() {
        return Flux.just(new IanaSource("application", new ClassPathResource("mediatypes-test.csv")));
    }
}
