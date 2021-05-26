package no.fdk.referencedata.mediatype;

import no.fdk.referencedata.mediatype.iana.IANAMediaTypeHarvester;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;

public class LocalMediaTypeHarvester extends IANAMediaTypeHarvester {
    @Override
    public Flux<Resource> getMediaTypesSources() {
        return Flux.just(new ClassPathResource("mediatypes-test.csv"));
    }
}
