package no.fdk.referencedata.digdir.qualitydimension;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QualityDimensionRepository extends JpaRepository<QualityDimension, String> {
    Optional<QualityDimension> findByCode(String code);
}
