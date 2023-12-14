package no.fdk.referencedata.linguisticsystem;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
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
import static org.mockito.Mockito.mock;

@ActiveProfiles("test")
public class LinguisticSystemServiceTest {

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_get_all_returns_all_linguistic_systems() {
        LinguisticSystemService service = new LinguisticSystemService(new LinguisticSystemImporter(), rdfSourceRepository);
        service.importLinguisticSystems();

        List<LinguisticSystem> languages = service.getAll();
        assertEquals(5, languages.size());

        LinguisticSystem first = languages.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/language/ENG", first.getUri());
        assertEquals("ENG", first.getCode());
        assertEquals("English", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_provenance_by_code_returns_correct_linguistic_system() {
        LinguisticSystemService service = new LinguisticSystemService(new LinguisticSystemImporter(), rdfSourceRepository);
        service.importLinguisticSystems();

        Optional<LinguisticSystem> languageOptional = service.getByCode("SMI");

        assertTrue(languageOptional.isPresent());

        LinguisticSystem language = languageOptional.get();
        assertEquals("http://publications.europa.eu/resource/authority/language/SMI", language.getUri());
        assertEquals("SMI", language.getCode());
        assertEquals("Sami languages", language.getLabel().get(Language.ENGLISH.code()));
    }

}
