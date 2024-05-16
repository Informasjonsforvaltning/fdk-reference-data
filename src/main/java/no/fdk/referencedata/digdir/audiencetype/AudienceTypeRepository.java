package no.fdk.referencedata.digdir.audiencetype;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AudienceTypeRepository extends CrudRepository<AudienceType, String> {
    Optional<AudienceType> findByCode(String code);
}
