package no.fdk.referencedata.eu.productstatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductStatusRepository extends JpaRepository<ProductStatus, String> {
    Optional<ProductStatus> findByCode(String code);
}
