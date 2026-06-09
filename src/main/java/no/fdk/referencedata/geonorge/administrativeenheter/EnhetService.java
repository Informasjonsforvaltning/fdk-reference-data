package no.fdk.referencedata.geonorge.administrativeenheter;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
import no.fdk.referencedata.search.SearchAlternative;
import no.fdk.referencedata.search.SearchHit;
import no.fdk.referencedata.search.SearchableReferenceData;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class EnhetService implements SearchableReferenceData {
    private final String rdfSourceID = "administrative-enheter-source";

    private final EnhetHarvester enhetHarvester;

    private final EnhetWriter enhetWriter;

    private final EnhetRepository enhetRepository;

    private final EnhetVariantRepository enhetVariantRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public EnhetService(
            EnhetHarvester enhetHarvester,
            EnhetRepository enhetRepository,
            EnhetVariantRepository enhetVariantRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository,
            EnhetWriter enhetWriter) {
        this.enhetHarvester = enhetHarvester;
        this.enhetRepository = enhetRepository;
        this.enhetVariantRepository = enhetVariantRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.enhetWriter = enhetWriter;
    }

    public boolean firstTime() {
        return enhetRepository.count() == 0;
    }

    public String getRdf(RDFFormat rdfFormat) {
        String source = rdfSourceRepository.findById(rdfSourceID).orElse(new RDFSource()).getTurtle();
        if (rdfFormat == RDFFormat.TURTLE) {
            return source;
        } else {
            return RDFUtils.modelToResponse(ModelFactory.createDefaultModel().read(source, Lang.TURTLE.getName()), rdfFormat);
        }
    }

    private void addEnhetToModel(Enhet enhet, Model model) {
        Resource resource = model.createResource(enhet.getUri());
        resource.addProperty(RDF.type, DCTerms.Location);
        if (enhet.name != null) {
            resource.addProperty(DCTerms.title, enhet.name);
        }
        if (enhet.code != null) {
            resource.addProperty(DCTerms.identifier, enhet.code);
        }
    }

    private void addEnhetVariantToModel(EnhetVariant enhet, Model model) {
        Resource resource = model.createResource(enhet.getUri());
        resource.addProperty(RDF.type, DCTerms.Location);
        if (enhet.name != null) {
            resource.addProperty(DCTerms.title, enhet.name);
        }
        if (enhet.code != null) {
            resource.addProperty(DCTerms.identifier, enhet.code);
        }
    }

    public SearchAlternative getSearchType() {
        return SearchAlternative.ADMINISTRATIVE_ENHETER;
    }

    public Stream<SearchHit> search(String query) {
        return enhetRepository.findByNameContainingIgnoreCase(query)
                .stream()
                .map(Enhet::toSearchHit);
    }

    public Stream<SearchHit> findByURIs(List<String> uris) {
        Stream<SearchHit> hits = enhetRepository.findByUriIn(uris)
                .stream()
                .map(Enhet::toSearchHit);

        Stream<SearchHit> variantHits = enhetVariantRepository.findByUriIn(uris)
                .stream()
                .map(EnhetVariant::toSearchHit);

        return Stream.concat(hits, variantHits);
    }

    private Stream<EnhetVariant> idVariantsOfEnhet(Enhet enhet) {
        // do not create variants if it's not possible to split the code into at least 2 chunks of size 6
        if (enhet.code.length() < 12) {
            return Stream.empty();
        }

        String uriBase = enhet.getUri().substring(0, enhet.getUri().lastIndexOf("/") + 1);
        List<String> codeVariants = new ArrayList<>();
        StringBuilder sb = new StringBuilder(enhet.code);

        // splits the code into chunks of 6 characters
        while (sb.length() > 5) {
            codeVariants.add(sb.substring(0, 6));
            sb.delete(0, 6);
        }

        return codeVariants.stream().map(codeVariant ->
                EnhetVariant.builder()
                        .uri(uriBase + codeVariant)
                        .name(enhet.name)
                        .code(codeVariant)
                        .build()
        );
    }

    public void harvestAndSave() {
        try {
            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.ADMINISTRATIVE_ENHETER.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.ADMINISTRATIVE_ENHETER.name())
                            .build());

            final List<Enhet> enheter = new ArrayList<>();
            enhetHarvester.harvest().toIterable().forEach(enheter::add);
            log.info("Harvest and saving {} administrative enheter", enheter.size());

            final List<EnhetVariant> docVariants = enheter.stream()
                    .map(enh -> EnhetVariant.builder()
                            .uri(enh.getUri().replace("/id/", "/doc/"))
                            .name(enh.getName())
                            .code(enh.getCode())
                            .build())
                    .toList();

            final List<EnhetVariant> idVariants = enheter.stream()
                    .flatMap(this::idVariantsOfEnhet)
                    .toList();

            Model model = ModelFactory.createDefaultModel();
            model.setNsPrefix("dct", DCTerms.NS);
            enheter.forEach(item -> addEnhetToModel(item, model));
            docVariants.forEach(item -> addEnhetVariantToModel(item, model));
            idVariants.forEach(item -> addEnhetVariantToModel(item, model));

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(rdfSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(model, RDFFormat.TURTLE));

            settings.setLatestHarvestDate(LocalDateTime.now());

            enhetWriter.replaceAll(enheter, docVariants, idVariants, rdfSource, settings);
        } catch (Exception e) {
            log.error("Unable to harvest administrative enheter", e);
        }
    }
}
