package no.fdk.referencedata.digdir.audiencetype;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AudienceTypeRepository extends CrudRepository<AudienceType, String> {
    Optional<AudienceType> findByCode(String code);
}
