package no.fdk.referencedata.digdir.servicechanneltype;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ServiceChannelTypes {
    List<ServiceChannelType> serviceChannelTypes;
}
