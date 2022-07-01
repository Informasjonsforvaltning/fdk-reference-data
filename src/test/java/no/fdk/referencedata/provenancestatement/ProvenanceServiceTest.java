package no.fdk.referencedata.provenancestatement;

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
public class ProvenanceServiceTest {

    @Test
    public void test_if_get_all_returns_all_provenance_statements() {
        ProvenanceStatementService service = new ProvenanceStatementService(new ProvenanceStatementImporter());
        service.importProvenanceStatements();

        List<ProvenanceStatement> provenanceStatements = service.getAll();
        assertEquals(4, provenanceStatements.size());

        ProvenanceStatement last = provenanceStatements.get(3);
        assertEquals("http://data.brreg.no/datakatalog/provinens/vedtak", last.getUri());
        assertEquals("VEDTAK", last.getCode());
        assertEquals("Governmental decisions", last.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_provenance_by_code_returns_correct_api_specification() {
        ProvenanceStatementService service = new ProvenanceStatementService(new ProvenanceStatementImporter());
        service.importProvenanceStatements();

        Optional<ProvenanceStatement> provenanceOptional = service.getByCode("TREDJEPART");

        assertTrue(provenanceOptional.isPresent());

        ProvenanceStatement provenance = provenanceOptional.get();
        assertEquals("http://data.brreg.no/datakatalog/provinens/tredjepart", provenance.getUri());
        assertEquals("TREDJEPART", provenance.getCode());
        assertEquals("Third party", provenance.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_provenance_model_is_isomorphic_with_old_rdf() {
        Model expected = ModelFactory.createDefaultModel();
        expected.read(requireNonNull(ProvenanceStatementImporter.class.getClassLoader().getResource("provenance.rdf")).toString());

        ProvenanceStatementService service = new ProvenanceStatementService(new ProvenanceStatementImporter());
        service.importProvenanceStatements();

        Model result = ModelFactory.createDefaultModel();
        result.read(new StringReader(service.getRdf(RDFFormat.TURTLE)), "http://base.example.com", "TURTLE");

        assertTrue(result.isIsomorphicWith(expected));
    }

}
