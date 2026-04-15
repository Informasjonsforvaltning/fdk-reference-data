package no.fdk.referencedata.digdir.audiencetype;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AudienceTypeRepository extends JpaRepository<AudienceType, String> {
    Optional<AudienceType> findByCode(String code);
}
