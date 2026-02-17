package no.fdk.referencedata.iana.mediatype;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface MediaTypeRepository extends CrudRepository<MediaType, String> {
    Stream<MediaType> findByUriIn(List<String> uris);
    List<MediaType> findByType(String type);
    Optional<MediaType> findByTypeAndSubType(String type, String subType);
    Stream<MediaType> findByNameContainingIgnoreCase(String query);
}
