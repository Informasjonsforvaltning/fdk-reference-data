package no.fdk.referencedata.digdir.serviceoutputtype;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ServiceOutputTypes {
    List<ServiceOutputType> serviceOutputTypes;
}
