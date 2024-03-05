package no.fdk.referencedata.referencetypes;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
public class ReferenceTypesServiceTest {

    @Test
    public void test_if_get_all_returns_all_reference_types() {
        ReferenceTypeService service = new ReferenceTypeService(new ReferenceTypeImporter());

        List<ReferenceType> referenceTypes = service.getAll();
        assertEquals(27, referenceTypes.size());

        ReferenceType first = referenceTypes.get(0);
        assertEquals("hasVersion", first.getCode());
        assertEquals("Has version", first.getLabel().get(Language.ENGLISH.code()));
        assertEquals("Is version of", first.getInverseLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_reference_type_by_code_returns_correct_reference_type() {
        ReferenceTypeService service = new ReferenceTypeService(new ReferenceTypeImporter());

        Optional<ReferenceType> typeOptional = service.getByCode("isReplacedBy");

        assertTrue(typeOptional.isPresent());

        ReferenceType referenceType = typeOptional.get();
        assertEquals("isReplacedBy", referenceType.getCode());
        assertEquals("Is replaced by", referenceType.getLabel().get(Language.ENGLISH.code()));
        assertEquals("Replaces", referenceType.getInverseLabel().get(Language.ENGLISH.code()));
    }

}
