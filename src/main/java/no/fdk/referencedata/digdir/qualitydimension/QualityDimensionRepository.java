package no.fdk.referencedata.digdir.qualitydimension;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface QualityDimensionRepository extends CrudRepository<QualityDimension, String> {
    Optional<QualityDimension> findByCode(String code);
}
