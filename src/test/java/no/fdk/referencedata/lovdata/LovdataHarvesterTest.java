package no.fdk.referencedata.lovdata;

import no.fdk.referencedata.core.HarvestParseException;
import no.fdk.referencedata.core.HarvestSourceException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LovdataHarvesterTest {

    private final LovdataHarvester harvester = new LovdataHarvester();

    private String header(String language, String... fields) {
        StringBuilder builder = new StringBuilder("<html lang=\"" + language + "\"><head>"
                + "<title>Tittel fra dokumenthode</title></head><body><dl>");
        for (int i = 0; i < fields.length; i += 2) {
            builder.append("<dd class=\"").append(fields[i]).append("\">")
                    .append(fields[i + 1]).append("</dd>");
        }
        return builder.append("</dl></body></html>").toString();
    }

    @Test
    void readsUriIdentifierAndLabel() {
        LegalResource resource = harvester.parse(header("nb",
                "eli", "/eli/lov/2008/05/15/35",
                "legacyID", "LOV-2008-05-15-35",
                "title", "Lov om utlendingers adgang til riket"), "lov");

        assertEquals("https://lovdata.no/eli/lov/2008/05/15/35", resource.getUri());
        assertEquals("LOV-2008-05-15-35", resource.getIdentifier());
        assertEquals(Map.of("nb", "Lov om utlendingers adgang til riket"), resource.getLabel());
        assertEquals("lov", resource.getType());
    }

    @Test
    void derivesUriFromDokidWhenEliIsMissing() {
        LegalResource resource = harvester.parse(header("nb",
                "dokid", "SF/forskrift/2008-12-22-1624",
                "title", "Forskrift om særlige hygieneregler"), "forskrift");

        assertEquals("https://lovdata.no/eli/forskrift/2008/12/22/1624", resource.getUri());
    }

    @Test
    void padsDerivedRunningNumberToTwoDigits() {
        LegalResource resource = harvester.parse(header("nb",
                "dokid", "NL/lov/1932-04-22-1",
                "title", "Lov"), "lov");

        assertEquals("https://lovdata.no/eli/lov/1932/04/22/01", resource.getUri());
    }

    @Test
    void usesZeroWhenDokidHasNoRunningNumber() {
        LegalResource resource = harvester.parse(header("nb",
                "dokid", "NL/lov/1920-02-09",
                "title", "Lov"), "lov");

        assertEquals("https://lovdata.no/eli/lov/1920/02/09/0", resource.getUri());
    }

    @Test
    void mapsMacrolanguageNorwegianToBokmaal() {
        LegalResource resource = harvester.parse(header("no",
                "eli", "/eli/lov/2000/01/01/01",
                "title", "Lov"), "lov");

        assertEquals(Map.of("nb", "Lov"), resource.getLabel());
    }

    @Test
    void keepsNynorskAsItsOwnLanguage() {
        LegalResource resource = harvester.parse(header("nn",
                "eli", "/eli/lov/1814/05/17/0",
                "title", "Kongeriket Noregs grunnlov"), "lov");

        assertEquals(Map.of("nn", "Kongeriket Noregs grunnlov"), resource.getLabel());
    }

    @Test
    void fallsBackToDocumentTitle() {
        LegalResource resource = harvester.parse(header("nb",
                "eli", "/eli/forskrift/2007/06/29/876"), "forskrift");

        assertEquals(Map.of("nb", "Tittel fra dokumenthode"), resource.getLabel());
    }

    @Test
    void skipsDocumentWithoutUri() {
        assertNull(harvester.parse(header("nb", "title", "Lov uten identifikator"), "lov"));
    }

    @Test
    void acceptsHarvestWhereNearlyEveryDocumentParses() {
        assertDoesNotThrow(() -> harvester.verifyShareParsed("url", "lov", 1000, 960));
    }

    @Test
    void rejectsHarvestWhereTooManyDocumentsFailToParse() {
        HarvestParseException thrown = assertThrows(HarvestParseException.class,
                () -> harvester.verifyShareParsed("url", "lov", 1000, 900));

        assertEquals("Only 900 of 1000 lov documents could be parsed, expected at least 95%."
                + " Has the Lovdata document structure changed?", thrown.getMessage());
    }

    @Test
    void rejectsEmptyArchive() {
        assertThrows(HarvestSourceException.class,
                () -> harvester.verifyShareParsed("url", "forskrift", 0, 0));
    }

    @Test
    void decodesHtmlEntitiesInTitle() {
        LegalResource resource = harvester.parse(header("nb",
                "eli", "/eli/lov/1958/06/06/2",
                "title", "Lov om pensjonsordning ved Den Norske Opera &amp; Ballett"), "lov");

        assertEquals("Lov om pensjonsordning ved Den Norske Opera & Ballett",
                resource.getLabel().get("nb"));
    }

    @Test
    void decodesNumericEntitiesAsPlainSpace() {
        assertEquals("Forskrift om kap. 3", harvester.decodeEntities("Forskrift om kap.&#xa0;3"));
        assertEquals("a b", harvester.decodeEntities("a&#32;b"));
    }

    @Test
    void decodesEachEntityOnce() {
        assertEquals("&lt;", harvester.decodeEntities("&amp;lt;"));
    }

    @Test
    void leavesUnknownEntitiesUntouched() {
        assertEquals("100 &unknown; verdi", harvester.decodeEntities("100 &unknown; verdi"));
    }
}
