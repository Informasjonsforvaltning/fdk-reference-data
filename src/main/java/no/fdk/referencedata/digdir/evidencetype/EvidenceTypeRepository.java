package no.fdk.referencedata.digdir.evidencetype;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvidenceTypeRepository extends CrudRepository<EvidenceType, String> {
    Optional<EvidenceType> findByCode(String code);
}
