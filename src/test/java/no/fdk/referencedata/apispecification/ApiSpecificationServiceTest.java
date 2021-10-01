package no.fdk.referencedata.apispecification;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.los.LosImporter;
import no.fdk.referencedata.los.LosNode;
import no.fdk.referencedata.los.LosService;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
public class ApiSpecificationServiceTest {

    @Test
    public void test_if_get_all_returns_all_api_specifications() {
        ApiSpecificationService service = new ApiSpecificationService(new ApiSpecificationImporter());
        service.importApiSpecifications();

        List<ApiSpecification> apiSpecifications = service.getAll();
        assertEquals(2, apiSpecifications.size());

        ApiSpecification first = apiSpecifications.get(0);
        assertEquals("https://data.norge.no/reference-data/api-specifications/account", first.getUri());
        assertEquals("account", first.getCode());
        assertEquals("https://bitsnorge.github.io/dsop-accounts-api", first.getSource());
        assertEquals("Account details", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_api_specifiction_by_code_returns_correct_api_specification() {
        ApiSpecificationService service = new ApiSpecificationService(new ApiSpecificationImporter());
        service.importApiSpecifications();

        Optional<ApiSpecification> apiSpecificationOptional = service.getByCode("customer-relationship");

        assertTrue(apiSpecificationOptional.isPresent());

        ApiSpecification spec = apiSpecificationOptional.get();
        assertEquals("https://data.norge.no/reference-data/api-specifications/customer-relationship", spec.getUri());
        assertEquals("customer-relationship", spec.getCode());
        assertEquals("https://bitsnorge.github.io/dsop-kfr-api", spec.getSource());
        assertEquals("Customer relationship", spec.getLabel().get(Language.ENGLISH.code()));
    }

}
