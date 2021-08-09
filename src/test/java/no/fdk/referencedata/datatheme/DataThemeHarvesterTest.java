package no.fdk.referencedata.datatheme;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DataThemeHarvesterTest {

    @Test
    public void test_fetch_datatypes() throws Exception {
        DataThemeHarvester dataThemeHarvester = new LocalDataThemeHarvester("20200923-0");

        assertNotNull(dataThemeHarvester.getDataThemesSource());
        assertEquals("data-theme-skos-ap-act.rdf", dataThemeHarvester.getDataThemesSource().getFilename());
        assertEquals("20200923-0", dataThemeHarvester.getVersion());

        List<DataTheme> dataThemes = dataThemeHarvester.harvestDataThemes().collectList().block();
        assertNotNull(dataThemes);
        assertEquals(14, dataThemes.size());

        DataTheme first = dataThemes.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/data-theme/TRAN", first.getUri());
        assertEquals("TRAN", first.getCode());
        assertEquals("Transport", first.getLabel().get(Language.ENGLISH.code()));
    }

}
