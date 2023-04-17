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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class LosService {
    private final String rdfSourceID = "los-source";

    public LosImporter losImporter;

    private final LosRepository losRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public LosService(LosImporter losImporter, LosRepository losRepository, RDFSourceRepository rdfSourceRepository) {
        this.losImporter = losImporter;
        this.losRepository = losRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public List<LosNode> getByURIs(List<String> uris) {
        return losRepository.findByUriIn(uris);
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
        return losRepository.findAll();
    }

    public boolean firstTime() { return losRepository.count() == 0; }

    @Transactional
    public void importLosNodes() {
        try {
            final List<LosNode> losList = losImporter.importFromLosSource();
            losRepository.deleteAll();
            losRepository.saveAll(losList);

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(rdfSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(losImporter.getModel(), RDFFormat.TURTLE));
            rdfSourceRepository.save(rdfSource);
        } catch(Exception e) {
            log.error("Unable to harvest LOS", e);
        }
    }
}
