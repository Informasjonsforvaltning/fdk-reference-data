package no.fdk.referencedata.digdir.qualitydimension;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QualityDimensionRepository extends CrudRepository<QualityDimension, String> {
    Optional<QualityDimension> findByCode(String code);
}
