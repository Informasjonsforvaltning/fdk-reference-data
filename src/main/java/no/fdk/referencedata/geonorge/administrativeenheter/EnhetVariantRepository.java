package no.fdk.referencedata.geonorge.administrativeenheter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
public interface EnhetVariantRepository extends JpaRepository<EnhetVariant, String> {
    Stream<EnhetVariant> findByUriIn(List<String> uris);
}
