package no.fdk.referencedata.digdir.highvaluedatasettype;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HighValueDatasetTypeRepository extends JpaRepository<HighValueDatasetType, String> {
    Optional<HighValueDatasetType> findByCode(String code);
}
