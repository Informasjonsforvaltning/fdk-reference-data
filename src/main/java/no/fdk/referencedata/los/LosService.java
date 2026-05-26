package no.fdk.referencedata.los;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class LosService {
    private final String rdfSourceID = "los-source";
    private final LosRepository losRepository;
    private final RDFSourceRepository rdfSourceRepository;
    private final LosWriter losWriter;
    public LosImporter losImporter;

    @Autowired
    public LosService(LosImporter losImporter, LosRepository losRepository, RDFSourceRepository rdfSourceRepository,
                      LosWriter losWriter) {
        this.losImporter = losImporter;
        this.losRepository = losRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.losWriter = losWriter;
    }

    public List<LosNode> getByURIs(List<String> uris) {
        return losRepository.findByUriIn(uris).stream()
                .sorted(Comparator.comparing(LosNode::getUri))
                .toList();
    }

    public String getRdf(RDFFormat rdfFormat) {
        String source = rdfSourceRepository.findById(rdfSourceID).orElse(new RDFSource()).getTurtle();
        if (rdfFormat == RDFFormat.TURTLE) {
            return source;
        } else {
            return RDFUtils.modelToResponse(ModelFactory.createDefaultModel().read(source, Lang.TURTLE.getName()), rdfFormat);
        }
    }

    public List<LosNode> getAll() {
        return losRepository.findAll().stream()
                .sorted(Comparator.comparing(LosNode::getUri))
                .toList();
    }

    public boolean firstTime() {
        return losRepository.count() == 0;
    }

    public void importLosNodes() {
        try {
            final List<LosNode> losList = losImporter.importFromLosSource();

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(rdfSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(losImporter.getModel(), RDFFormat.TURTLE));

            losWriter.replaceAll(losList, rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest LOS", e);
        }
    }
}
