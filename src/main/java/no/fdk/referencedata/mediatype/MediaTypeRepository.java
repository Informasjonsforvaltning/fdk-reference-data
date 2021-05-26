package no.fdk.referencedata.mediatype;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface MediaTypeRepository extends CrudRepository<MediaType, String> {
    List<MediaType> findByType(String type);

    Optional<MediaType> findByTypeAndSubType(String type, String subType);
}