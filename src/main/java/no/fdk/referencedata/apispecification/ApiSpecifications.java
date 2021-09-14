package no.fdk.referencedata.apispecification;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ApiSpecifications {
    List<ApiSpecification> apiSpecifications;
}
