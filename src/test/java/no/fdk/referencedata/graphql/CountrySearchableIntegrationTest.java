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
class CountrySearchableIntegrationTest extends AbstractContainerTest {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private ContinentRepository continentRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private GraphQlTester graphQlTester;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        CountryService countryService = new CountryService(
                new LocalCountryHarvester(),
                countryRepository,
                rdfSourceRepository,
                new CountryWriter(countryRepository, rdfSourceRepository));

        countryService.harvestAndSave();

        ContinentService continentService = new ContinentService(
                new LocalContinentHarvester(),
                continentRepository,
                rdfSourceRepository,
                new ContinentWriter(continentRepository, rdfSourceRepository));

        continentService.harvestAndSave();
    }

    @Test
    void test_if_search_query_returns_country_hit() {
        SearchRequest req = SearchRequest.builder().query("Norway").types(List.of(EU_LOCATIONS)).build();
        List<SearchHit> result = graphQlTester.documentName("search")
                .variable("req", objectMapper.convertValue(req, Map.class))
                .execute()
                .path("$['data']['search']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(1, result.size());

        SearchHit hit = result.get(0);

        assertEquals("http://publications.europa.eu/resource/authority/country/NOR", hit.getUri());
        assertEquals("NOR", hit.getCode());
        assertEquals("Norway", hit.getLabel().get("en"));
        assertEquals(EU_LOCATIONS, hit.getType());
    }

    @Test
    void test_if_that_hits_that_starts_with_search_query_is_prioritized_in_sort() {
        // "no" matches all EU_LOCATIONS entries via the JSONB "no" label key they share;
        // only NOR's Norwegian label ("Norge") starts with "no", so it sorts first
        SearchRequest req = SearchRequest.builder().query("no").types(List.of(EU_LOCATIONS)).build();
        List<SearchHit> result = graphQlTester.documentName("search")
                .variable("req", objectMapper.convertValue(req, Map.class))
                .execute()
                .path("$['data']['search']")
                .entityList(SearchHit.class)
                .get();

        assertEquals(6, result.size());

        assertEquals("http://publications.europa.eu/resource/authority/country/NOR", result.get(0).getUri());
        assertEquals("NOR", result.get(0).getCode());
        assertEquals("Norway", result.get(0).getLabel().get("en"));
        assertEquals(EU_LOCATIONS, result.get(0).getType());
    }

    @Test
    void test_if_find_by_uris_query_returns_country_hits() {
        List<String> expectedURIs = List.of(
                "http://publications.europa.eu/resource/authority/country/NOR",
                "http://publications.europa.eu/resource/authority/country/DEU"
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
