package no.fdk.referencedata.adms.publishertype;

import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface PublisherTypeRepository extends ListCrudRepository<PublisherType, String> {
    Optional<PublisherType> findByCode(String code);
}
