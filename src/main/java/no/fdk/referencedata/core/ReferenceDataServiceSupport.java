package no.fdk.referencedata.core;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Component
@Slf4j
public class ReferenceDataServiceSupport {

    private final ReferenceDataWriter referenceDataWriter;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public ReferenceDataServiceSupport(
            ReferenceDataWriter referenceDataWriter,
            RDFSourceRepository rdfSourceRepository) {
        this.referenceDataWriter = referenceDataWriter;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime(JpaRepository<?, String> repository) {
        return repository.count() == 0;
    }

    public String getRdf(String sourceId, RDFFormat rdfFormat) {
        String source = rdfSourceRepository.findById(sourceId).orElse(new RDFSource()).getTurtle();
        if (rdfFormat == RDFFormat.TURTLE) {
            return source;
        }
        return RDFUtils.modelToResponse(
                ModelFactory.createDefaultModel().read(source, Lang.TURTLE.getName()),
                rdfFormat);
    }

    public <T> void harvestAndSave(
            ModelHarvester<T> harvester,
            JpaRepository<T, String> repository,
            String sourceId,
            String logName) {
        try {
            final List<T> items = new ArrayList<>();
            harvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} {}", items.size(), logName);

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(sourceId);
            rdfSource.setTurtle(RDFUtils.modelToResponse(harvester.getModel(), RDFFormat.TURTLE));

            referenceDataWriter.replaceAll(repository, items, rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest {}", logName, e);
        }
    }

    public <T> void harvestAndSaveWithoutRdf(
            Supplier<Flux<T>> harvest,
            JpaRepository<T, String> repository,
            String logName) {
        try {
            final List<T> items = new ArrayList<>();
            harvest.get().toIterable().forEach(items::add);
            log.info("Harvest and saving {} {}", items.size(), logName);
            referenceDataWriter.replaceAll(repository, items);
        } catch (Exception e) {
            log.error("Unable to harvest {}", logName, e);
        }
    }

    public <T> void saveAll(
            List<T> items,
            Model model,
            JpaRepository<T, String> repository,
            String sourceId) {
        RDFSource rdfSource = new RDFSource();
        rdfSource.setId(sourceId);
        rdfSource.setTurtle(RDFUtils.modelToResponse(model, RDFFormat.TURTLE));
        referenceDataWriter.replaceAll(repository, items, rdfSource);
    }
}
