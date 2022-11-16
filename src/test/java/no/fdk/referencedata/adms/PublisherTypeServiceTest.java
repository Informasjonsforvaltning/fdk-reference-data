package no.fdk.referencedata.adms;

import no.fdk.referencedata.adms.publishertype.PublisherType;
import no.fdk.referencedata.adms.publishertype.PublisherTypeImporter;
import no.fdk.referencedata.adms.publishertype.PublisherTypeService;
import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
public class PublisherTypeServiceTest {

    @Test
    public void test_if_get_all_returns_all_publisher_types() {
        PublisherTypeService service = new PublisherTypeService(new PublisherTypeImporter());
        service.importPublisherTypes();

        List<PublisherType> publisherTypes = service.getAll();
        assertEquals(11, publisherTypes.size());

        PublisherType first = publisherTypes.get(0);
        assertEquals("http://purl.org/adms/publishertype/Academia-ScientificOrganisation", first.getUri());
        assertEquals("Academia-ScientificOrganisation", first.getCode());
        assertEquals("Academia/Scientific organisation", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_by_code_returns_correct_publisher_type() {
        PublisherTypeService service = new PublisherTypeService(new PublisherTypeImporter());
        service.importPublisherTypes();

        Optional<PublisherType> publisherTypeOptional = service.getByCode("NonProfitOrganisation");

        assertTrue(publisherTypeOptional.isPresent());

        PublisherType publisherType = publisherTypeOptional.get();
        assertEquals("http://purl.org/adms/publishertype/NonProfitOrganisation", publisherType.getUri());
        assertEquals("NonProfitOrganisation", publisherType.getCode());
        assertEquals("Non-Profit Organisation", publisherType.getLabel().get(Language.ENGLISH.code()));
    }

}
