package no.fdk.referencedata.digdir.conceptsubjects;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class ConceptSubjectService {

    private final ConceptSubjectHarvester conceptSubjectHarvester;

    private final ConceptSubjectRepository conceptSubjectRepository;

    @Autowired
    public ConceptSubjectService(ConceptSubjectHarvester conceptSubjectHarvester,
                                 ConceptSubjectRepository conceptSubjectRepository) {
        this.conceptSubjectHarvester = conceptSubjectHarvester;
        this.conceptSubjectRepository = conceptSubjectRepository;
    }

    public boolean firstTime() {
        return conceptSubjectRepository.count() == 0;
    }

    @Transactional
    public void harvestAndSave() {
        try {
            conceptSubjectRepository.deleteAll();

            final AtomicInteger counter = new AtomicInteger(0);
            final Iterable<ConceptSubject> iterable = conceptSubjectHarvester.harvest().toIterable();
            iterable.forEach(item -> counter.getAndIncrement());
            log.info("Harvest and saving {} concept subjects", counter.get());
            conceptSubjectRepository.saveAll(iterable);

        } catch(Exception e) {
            log.error("Unable to harvest concept subjects", e);
        }
    }
}
