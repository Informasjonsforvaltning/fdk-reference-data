package no.fdk.referencedata.geonames;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class GeonamesWriter {

    private final GeonamesFylkeRepository geonamesFylkeRepository;
    private final GeonamesKommuneRepository geonamesKommuneRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public GeonamesWriter(
            GeonamesFylkeRepository geonamesFylkeRepository,
            GeonamesKommuneRepository geonamesKommuneRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.geonamesFylkeRepository = geonamesFylkeRepository;
        this.geonamesKommuneRepository = geonamesKommuneRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<GeonamesFylke> fylker, List<GeonamesKommune> kommuner, RDFSource rdfSource) {
        geonamesKommuneRepository.deleteAll();
        geonamesFylkeRepository.deleteAll();
        geonamesFylkeRepository.saveAll(fylker);
        geonamesKommuneRepository.saveAll(kommuner);
        rdfSourceRepository.save(rdfSource);
    }
}
