package no.fdk.referencedata.geonorge.administrativeenheter;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.stream.Stream;

public interface EnhetVariantRepository extends CrudRepository<EnhetVariant, String> {
    Stream<EnhetVariant> findByUriIn(List<String> uris);
}
