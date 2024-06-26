package no.fdk.referencedata.eu.datatheme;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static no.fdk.referencedata.eu.datatheme.LocalDataThemeHarvester.DATA_THEMES_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class DataThemeHarvesterTest {

    @Test
    public void test_fetch_datatypes() throws Exception {
        DataThemeHarvester dataThemeHarvester = new LocalDataThemeHarvester("20200923-0");

        assertNotNull(dataThemeHarvester.getSource());
        assertEquals("data-theme-sparql-result.ttl", dataThemeHarvester.getSource().getFilename());
        assertEquals("20200923-0", dataThemeHarvester.getVersion());

        List<DataTheme> dataThemes = dataThemeHarvester.harvest().collectList().block();
        assertNotNull(dataThemes);
        assertEquals(DATA_THEMES_SIZE, dataThemes.size());

        DataTheme first = dataThemes.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/data-theme/SOCI", first.getUri());
        assertEquals("SOCI", first.getCode());
        assertEquals("Population and society", first.getLabel().get(Language.ENGLISH.code()));
        assertEquals(LocalDate.parse("2015-10-01"), first.getStartUse());
        assertEquals("http://publications.europa.eu/resource/authority/data-theme", first.getConceptSchema().getUri());
        assertEquals("Data theme", first.getConceptSchema().getLabel().get(Language.ENGLISH.code()));
        assertEquals("20220715-0", first.getConceptSchema().getVersionNumber());
    }

}
