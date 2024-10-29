package no.fdk.referencedata.geonorge.administrativeenheter;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalEnhetHarvester extends EnhetHarvester {

    public static final int ADMINISTRATIVE_ENHETER_SIZE = 31;

    public LocalEnhetHarvester() {}

    @Override
    public Resource getSource() {
        return new ClassPathResource("administrative-enheter-harvest.ttl");
    }
}
