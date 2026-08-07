package no.fdk.referencedata.iana.mediatype;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;
import no.fdk.referencedata.search.SearchAlternative;
import no.fdk.referencedata.search.SearchHit;
import no.fdk.referencedata.search.SearchableReferenceData;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class MediaTypeService implements SearchableReferenceData, HarvestableReferenceData {
    private final String rdfSourceID = "media-types-source";

    private final MediaTypeHarvester mediaTypeHarvester;
    private final MediaTypeRepository mediaTypeRepository;
    private final ReferenceDataServiceSupport support;

    @Autowired
    public MediaTypeService(
            MediaTypeHarvester mediaTypeHarvester,
            MediaTypeRepository mediaTypeRepository,
            ReferenceDataServiceSupport support) {
        this.mediaTypeHarvester = mediaTypeHarvester;
        this.mediaTypeRepository = mediaTypeRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(mediaTypeRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(rdfSourceID, rdfFormat);
    }

    private void addMediaTypeToModel(MediaType mediaType, Model model) {
        Resource resource = model.createResource(mediaType.getUri());
        resource.addProperty(RDF.type, DCTerms.MediaType);
        if (mediaType.name != null) {
            resource.addProperty(DCTerms.title, mediaType.name);
        }
        if (mediaType.type != null && mediaType.subType != null) {
            resource.addProperty(DCTerms.identifier, mediaType.type + "/" + mediaType.subType);
        } else if (mediaType.type != null) {
            resource.addProperty(DCTerms.identifier, mediaType.type);
        }
    }

    public SearchAlternative getSearchType() {
        return SearchAlternative.IANA_MEDIA_TYPES;
    }

    public Stream<SearchHit> search(String query) {
        return mediaTypeRepository.findByNameContainingIgnoreCase(query)
                .stream()
                .map(MediaType::toSearchHit);
    }

    public Stream<SearchHit> findByURIs(List<String> uris) {
        return mediaTypeRepository.findByUriIn(uris)
                .stream()
                .map(MediaType::toSearchHit);
    }

    @Override
    public void harvestAndSave() {
        try {
            final List<MediaType> items = new ArrayList<>();
            mediaTypeHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} media-types", items.size());

            Model model = ModelFactory.createDefaultModel();
            model.setNsPrefix("dct", DCTerms.NS);
            items.forEach(item -> addMediaTypeToModel(item, model));

            support.saveAll(items, model, mediaTypeRepository, rdfSourceID);
        } catch (Exception e) {
            log.error("Unable to harvest media-types", e);
        }
    }
}
