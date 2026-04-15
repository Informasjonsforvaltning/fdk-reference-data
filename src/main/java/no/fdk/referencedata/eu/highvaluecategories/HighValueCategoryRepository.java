package no.fdk.referencedata.eu.highvaluecategories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HighValueCategoryRepository extends JpaRepository<HighValueCategory, String> {
    Optional<HighValueCategory> findByCode(String code);
}
