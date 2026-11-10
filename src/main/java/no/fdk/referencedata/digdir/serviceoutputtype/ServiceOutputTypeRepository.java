package no.fdk.referencedata.digdir.serviceoutputtype;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceOutputTypeRepository extends JpaRepository<ServiceOutputType, String> {
    Optional<ServiceOutputType> findByCode(String code);
}
