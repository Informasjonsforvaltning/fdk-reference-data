package no.fdk.referencedata.digdir.servicechanneltype;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceChannelTypeRepository extends JpaRepository<ServiceChannelType, String> {
    Optional<ServiceChannelType> findByCode(String code);
}
