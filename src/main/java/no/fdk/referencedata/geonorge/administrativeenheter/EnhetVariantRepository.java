package no.fdk.referencedata.geonorge.administrativeenheter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnhetVariantRepository extends JpaRepository<EnhetVariant, String> {
    List<EnhetVariant> findByUriIn(List<String> uris);
}
