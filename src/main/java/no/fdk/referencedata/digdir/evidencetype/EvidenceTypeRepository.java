package no.fdk.referencedata.digdir.evidencetype;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EvidenceTypeRepository extends CrudRepository<EvidenceType, String> {
    Optional<EvidenceType> findByCode(String code);
}
