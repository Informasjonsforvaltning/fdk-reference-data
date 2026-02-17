package no.fdk.referencedata.adms.publishertype;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublisherTypeRepository extends ListCrudRepository<PublisherType, String> {
    Optional<PublisherType> findByCode(String code);
}
