package no.fdk.referencedata.referencetypes;

import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFFormat;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
public class ReferenceTypesServiceTest {

    @Test
    public void test_if_get_all_returns_all_reference_types() {
        ReferenceTypeService service = new ReferenceTypeService(new ReferenceTypeImporter());
        service.importReferenceTypes();

        List<ReferenceType> referenceTypes = service.getAll();
        assertEquals(11, referenceTypes.size());

        ReferenceType first = referenceTypes.get(0);
        assertEquals("http://purl.org/dc/terms/hasVersion", first.getUri());
        assertEquals("hasVersion", first.getCode());
        assertEquals("Has version", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_reference_type_by_code_returns_correct_reference_type() {
        ReferenceTypeService service = new ReferenceTypeService(new ReferenceTypeImporter());
        service.importReferenceTypes();

        Optional<ReferenceType> typeOptional = service.getByCode("isReplacedBy");

        assertTrue(typeOptional.isPresent());

        ReferenceType referenceType = typeOptional.get();
        assertEquals("http://purl.org/dc/terms/isReplacedBy", referenceType.getUri());
        assertEquals("isReplacedBy", referenceType.getCode());
        assertEquals("Is replaced by", referenceType.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_reference_types_model_is_isomorphic_with_old_rdf() {
        Model expected = ModelFactory.createDefaultModel();
        expected.read(requireNonNull(ReferenceTypesServiceTest.class.getClassLoader().getResource("reference-code-skos.ttl")).toString());

        ReferenceTypeService service = new ReferenceTypeService(new ReferenceTypeImporter());
        service.importReferenceTypes();

        Model result = ModelFactory.createDefaultModel();
        result.read(new StringReader(service.getRdf(RDFFormat.TURTLE)), "http://base.example.com", "TURTLE");

        assertTrue(result.isIsomorphicWith(expected));
    }

}
