package no.fdk.referencedata.eu.language;

import no.fdk.referencedata.LocalHarvesters;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.Comparator;
import java.util.List;

import static no.fdk.referencedata.LocalHarvestFixtures.LANGUAGES_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class LanguageHarvesterTest {

    @Test
    public void test_fetch_languages() {
        LanguageHarvester harvester = LocalHarvesters.language();

        assertNotNull(harvester.getSource());
        assertEquals("language-sparql-result.ttl", harvester.getSource().getFilename());

        List<Language> languages = harvester.harvest().collectList().block();
        assertNotNull(languages);
        assertEquals(LANGUAGES_SIZE, languages.size());

        languages.sort(Comparator.comparing(Language::getUri));
        Language first = languages.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/language/ENG", first.getUri());
        assertEquals("ENG", first.getCode());
        assertEquals("English", first.getLabel().get(no.fdk.referencedata.i18n.Language.ENGLISH.code()));
        assertEquals("engelsk", first.getLabel().get(no.fdk.referencedata.i18n.Language.NORWEGIAN.code()));

        Language nob = languages.stream()
                .filter(language -> language.getCode().equals("NOB"))
                .findFirst()
                .orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/language/NOB", nob.getUri());
        assertEquals("NOB", nob.getCode());
        assertEquals("Norwegian Bokmål", nob.getLabel().get(no.fdk.referencedata.i18n.Language.ENGLISH.code()));
        assertEquals("norsk (bokmål)", nob.getLabel().get(no.fdk.referencedata.i18n.Language.NORWEGIAN.code()));
    }
}
