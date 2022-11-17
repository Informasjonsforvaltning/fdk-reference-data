package no.fdk.referencedata.digdir.servicechanneltype;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ServiceChannelTypeRepository extends CrudRepository<ServiceChannelType, String> {
    Optional<ServiceChannelType> findByCode(String code);
}
