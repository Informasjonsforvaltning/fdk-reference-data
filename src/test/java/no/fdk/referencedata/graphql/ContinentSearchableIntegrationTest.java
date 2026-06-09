package no.fdk.referencedata.graphql;

import no.fdk.referencedata.eu.country.CountryWriter;
import no.fdk.referencedata.eu.continent.ContinentWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.eu.continent.ContinentRepository;
import no.fdk.referencedata.eu.continent.ContinentService;
import no.fdk.referencedata.eu.continent.LocalContinentHarvester;
import no.fdk.referencedata.eu.country.CountryRepository;
import no.fdk.referencedata.eu.country.CountryService;
import no.fdk.referencedata.eu.country.LocalCountryHarvester;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.search.FindByURIsRequest;
import no.fdk.referencedata.search.SearchHit;
import no.fdk.referencedata.search.SearchRequest;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static no.fdk.referencedata.search.SearchAlternative.EU_LOCATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class ContinentSearchableIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ContinentRepository continentRepository;

    @Autowired
    private CountryRepository countryRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private GraphQlTester graphQlTester;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        ContinentService continentService = new ContinentService(
                new LocalContinentHarvester("1"),
                continentRepository,
                rdfSourceRepository,
                harvestSettingsRepository,
                new ContinentWriter(continentRepository, rdfSourceRepository, harvestSettingsRepository));

        continentService.harvestAndSave();

        CountryService countryService = new CountryService(
                new LocalCountryHarvester("1"),
                countryRepository,
                rdfSourceRepository,
                harvestSettingsRepository,
                new CountryWriter(countryRepository, rdfSourceRepository, harvestSettingsRepository));

        countryService.harvestAndSave();
    }

    @Test
    void test_if_search_query_returns_continent_hit() {
        SearchRequest req = SearchRequest.builder().query("Africa").types(List.of(EU_LOCATIONS)).build();
        List<SearchHit> result = graphQlTester.documentName("search")
                .variable("req", objectMapper.convertValue(req, Map.class))
                .execute()
                .path("$['data']['search']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(1, result.size());

        SearchHit hit = result.get(0);

        assertEquals("http://publications.europa.eu/resource/authority/continent/AFRICA", hit.getUri());
        assertEquals("AFRICA", hit.getCode());
        assertEquals("Africa", hit.getLabel().get("en"));
        assertEquals(EU_LOCATIONS, hit.getType());
    }

    @Test
    void test_if_that_hits_that_starts_with_search_query_is_prioritized_in_sort() {
        // "a" matches all EU_LOCATIONS entries; continents with Norwegian labels starting with
        // "a" (Afrika, Asia) sort before those without (Europa, Frankrike, Norge, Tyskland)
        SearchRequest req = SearchRequest.builder().query("a").types(List.of(EU_LOCATIONS)).build();
        List<SearchHit> result = graphQlTester.documentName("search")
                .variable("req", objectMapper.convertValue(req, Map.class))
                .execute()
                .path("$['data']['search']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(6, result.size());

        assertEquals("http://publications.europa.eu/resource/authority/continent/AFRICA", result.get(0).getUri());
        assertEquals("AFRICA", result.get(0).getCode());
        assertEquals("Africa", result.get(0).getLabel().get("en"));
        assertEquals(EU_LOCATIONS, result.get(0).getType());

        assertEquals("http://publications.europa.eu/resource/authority/continent/ASIA", result.get(1).getUri());
        assertEquals("ASIA", result.get(1).getCode());
        assertEquals("Asia", result.get(1).getLabel().get("en"));
        assertEquals(EU_LOCATIONS, result.get(1).getType());
    }

    @Test
    void test_if_find_by_uris_query_returns_continent_hits() {
        List<String> expectedURIs = List.of(
                "http://publications.europa.eu/resource/authority/continent/AFRICA",
                "http://publications.europa.eu/resource/authority/continent/EUROPE"
        );
        FindByURIsRequest req = FindByURIsRequest.builder().uris(expectedURIs).types(List.of(EU_LOCATIONS)).build();

        List<SearchHit> actual = graphQlTester.documentName("find-by-uris")
                .variable("req", objectMapper.convertValue(req, Map.class))
                .execute()
                .path("$['data']['findByURIs']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(2, actual.size());

        List<String> actualURIs = Stream.of(
                actual.get(0).getUri(),
                actual.get(1).getUri()
        ).sorted().toList();

        assertEquals(expectedURIs.stream().sorted().toList(), actualURIs);
    }
}
