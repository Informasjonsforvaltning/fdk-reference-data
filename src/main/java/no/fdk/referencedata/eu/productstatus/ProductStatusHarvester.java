package no.fdk.referencedata.eu.productstatus;

import no.fdk.referencedata.eu.GenericEuSkosHarvester;
import no.fdk.referencedata.eu.vocabulary.EUProductStatus;
import no.fdk.referencedata.rdf.SkosMapper;
import org.apache.jena.rdf.model.Resource;
import org.springframework.stereotype.Component;

@Component
public class ProductStatusHarvester extends GenericEuSkosHarvester<ProductStatus> {

    @Override
    protected String schemaName() {
        return "product-status";
    }

    @Override
    protected Resource scheme() {
        return EUProductStatus.SCHEME;
    }

    @Override
    protected String logName() {
        return "product statuses";
    }

    @Override
    protected ProductStatus mapConcept(Resource productStatus) {
        return ProductStatus.builder()
                .uri(productStatus.getURI())
                .code(extractCode(productStatus))
                .label(SkosMapper.extractLabels(productStatus))
                .build();
    }
}
