package no.fdk.referencedata.geonorge.administrativeenheter.nasjon;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.rdf.RDFUtils;
import no.fdk.referencedata.search.SearchAlternative;
import no.fdk.referencedata.search.SearchHit;
import no.fdk.referencedata.search.SearchableReferenceData;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Slf4j
public class NasjonService implements SearchableReferenceData {
    private final static Nasjon NORGE = Nasjon.builder()
            .uri("https://data.geonorge.no/administrativeEnheter/nasjon/id/173163")
            .nasjonsnavn("Norge")
            .nasjonsnummer("173163")
            .build();

    public SearchAlternative getSearchType() {
        return SearchAlternative.ADMINISTRATIVE_ENHETER;
    }

    public List<Nasjon> getNasjoner() {
        return List.of(NORGE);
    }

    public Optional<Nasjon> getNasjonByNasjonsnummer(String nasjonsnummer) {
        return getNasjoner().stream()
                .filter(nasjon -> nasjon.getNasjonsnummer().equals(nasjonsnummer))
                .findFirst();
    }

    public Stream<SearchHit> search(String query) {
        if (NORGE.getNasjonsnavn().toLowerCase().contains(query.toLowerCase())) {
            return Stream.of(NORGE.toSearchHit());
        }
        return Stream.empty();
    }

    public Stream<SearchHit> findByURIs(List<String> uris) {
        if (uris.contains(NORGE.uri)) {
            return Stream.of(NORGE.toSearchHit());
        }
        return Stream.empty();
    }

    public String getRdf(RDFFormat rdfFormat) {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("dct", DCTerms.NS);
        Resource norge = model.createResource(NORGE.uri);
        norge.addProperty(RDF.type, DCTerms.Location);
        norge.addProperty(DCTerms.title, NORGE.nasjonsnavn);
        norge.addProperty(DCTerms.identifier, NORGE.nasjonsnummer);
        return RDFUtils.modelToResponse(model, rdfFormat);
    }
}
