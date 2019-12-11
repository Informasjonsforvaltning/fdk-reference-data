package no.dcat.themes.builders;

import no.dcat.datastore.domain.dcat.builders.RdfModelLoader;
import no.dcat.shared.SkosCode;
import no.dcat.shared.Types;
import no.dcat.themes.database.TDBConnection;
import no.dcat.themes.database.TDBInferenceService;
import no.dcat.themes.database.TDBService;
import no.dcat.themes.service.CodesService;
import no.fdk.test.testcategories.IntegrationTest;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Tests to check that location uris can be handled correctly by reference data service.
 */
@Category(IntegrationTest.class)
public class LocationIT {
    private static Logger logger = LoggerFactory.getLogger(LocationIT.class);

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private CodesService codesService;


    @Before
    public void setup() throws IOException {
        TDBService tdbService = new TDBService(testFolder.getRoot().getCanonicalPath());
        tdbService.postConstruct();

        TDBInferenceService tdbInferenceService = new TDBInferenceService(tdbService);
        TDBConnection tdbConnection = new TDBConnection(tdbInferenceService);

        codesService = new CodesService(tdbConnection);
    }

    @Test
    public void testNorwayFromGeonames() throws IOException {
        SkosCode code = codesService.addLocation("http://sws.geonames.org/3144096/");

        assertEquals("Norge", code.getPrefLabel().get("no"));
    }

    @Test
    public void testOsloFromGeonames2() throws IOException {

        SkosCode code = codesService.addLocation("http://sws.geonames.org/3143242/");

        assertEquals("Oslo", code.getPrefLabel().get("no"));
    }

}
