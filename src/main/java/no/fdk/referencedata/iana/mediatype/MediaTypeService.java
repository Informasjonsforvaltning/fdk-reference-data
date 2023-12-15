package no.fdk.referencedata.iana.mediatype;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class MediaTypeService {
    private final String rdfSourceID = "media-types-source";

    private final MediaTypeHarvester mediaTypeHarvester;

    private final MediaTypeRepository mediaTypeRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public MediaTypeService(MediaTypeHarvester mediaTypeHarvester,
                            MediaTypeRepository mediaTypeRepository,
                            RDFSourceRepository rdfSourceRepository,
                            HarvestSettingsRepository harvestSettingsRepository) {
        this.mediaTypeHarvester = mediaTypeHarvester;
        this.mediaTypeRepository = mediaTypeRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime() {
        return mediaTypeRepository.count() == 0;
    }

    public String getRdf(RDFFormat rdfFormat) {
        String source = rdfSourceRepository.findById(rdfSourceID).orElse(new RDFSource()).getTurtle();
        if (rdfFormat == RDFFormat.TURTLE) {
            return source;
        } else {
            return RDFUtils.modelToResponse(ModelFactory.createDefaultModel().read(source, Lang.TURTLE.getName()), rdfFormat);
        }
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

    @Transactional
    public void harvestAndSave() {
        try {
            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.MEDIA_TYPE.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.MEDIA_TYPE.name())
                            .latestVersion("0")
                            .build());

            mediaTypeRepository.deleteAll();

            final AtomicInteger counter = new AtomicInteger(0);
            final Iterable<MediaType> iterable = mediaTypeHarvester.harvest().toIterable();
            iterable.forEach(item -> counter.getAndIncrement());
            log.info("Harvest and saving {} media-types", counter.get());
            mediaTypeRepository.saveAll(iterable);

            settings.setLatestHarvestDate(LocalDateTime.now());
            harvestSettingsRepository.save(settings);

            Model model = ModelFactory.createDefaultModel();
            model.setNsPrefix("dct", DCTerms.NS);
            iterable.forEach(item -> addMediaTypeToModel(item, model));

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(rdfSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(model, RDFFormat.TURTLE));
            rdfSourceRepository.save(rdfSource);
        } catch(Exception e) {
            log.error("Unable to harvest media-types", e);
        }
    }
}
