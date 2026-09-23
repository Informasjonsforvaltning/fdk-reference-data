package no.fdk.referencedata.eu.productstatus;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductStatuses {
    List<ProductStatus> productStatuses;
}
