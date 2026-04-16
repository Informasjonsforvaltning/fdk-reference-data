package no.fdk.referencedata.provenancestatement;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ActiveProfiles("test")
public class ProvenanceServiceTest {

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_get_all_returns_all_provenance_statements() {
        ProvenanceStatementService service = new ProvenanceStatementService(new ProvenanceStatementImporter(), rdfSourceRepository);
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
        ProvenanceStatementService service = new ProvenanceStatementService(new ProvenanceStatementImporter(), rdfSourceRepository);
        service.importProvenanceStatements();

        Optional<ProvenanceStatement> provenanceOptional = service.getByCode("TREDJEPART");

        assertTrue(provenanceOptional.isPresent());

        ProvenanceStatement provenance = provenanceOptional.get();
        assertEquals("http://data.brreg.no/datakatalog/provinens/tredjepart", provenance.getUri());
        assertEquals("TREDJEPART", provenance.getCode());
        assertEquals("Third party", provenance.getLabel().get(Language.ENGLISH.code()));
    }

}
