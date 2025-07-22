package no.fdk.referencedata.eu.licence;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.Comparator;
import java.util.List;

import static no.fdk.referencedata.eu.licence.LocalLicenceHarvester.LICENCES_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class LicenceHarvesterTest {

    @Test
    public void test_fetch_licences() {
        LicenceHarvester harvester = new LocalLicenceHarvester("20240610-0");

        assertNotNull(harvester.getSource());
        assertEquals("licences-sparql-result.ttl", harvester.getSource().getFilename());
        assertEquals("20240610-0", harvester.getVersion());

        List<Licence> licences = harvester.harvest().collectList().block();
        assertNotNull(licences);
        assertEquals(LICENCES_SIZE, licences.size());

        licences.sort(Comparator.comparing(Licence::getUri));
        Licence first = licences.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/licence/0BSD", first.getUri());
        assertEquals("0BSD", first.getCode());
        assertEquals("Zero-Clause BSD / Free Public License 1.0.0 (0BSD)", first.getLabel().get(Language.ENGLISH.code()));
        assertEquals(false, first.deprecated);
        assertEquals("The BSD 0-clause licence, 0BSD, goes further than the 2-clause licence by dropping the requirements to include the copyright notice, licence text or disclaimer in either source or binary forms. Doing so forms a public-domain-equivalent licence. It is approved by the Open Source Initiative.", first.definition.get(Language.ENGLISH.code()));
    }

} 
