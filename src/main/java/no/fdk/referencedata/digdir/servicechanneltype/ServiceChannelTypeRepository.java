package no.fdk.referencedata.digdir.servicechanneltype;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceChannelTypeRepository extends CrudRepository<ServiceChannelType, String> {
    Optional<ServiceChannelType> findByCode(String code);
}
