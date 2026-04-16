package no.fdk.referencedata.digdir.evidencetype;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvidenceTypeRepository extends JpaRepository<EvidenceType, String> {
    Optional<EvidenceType> findByCode(String code);
}
