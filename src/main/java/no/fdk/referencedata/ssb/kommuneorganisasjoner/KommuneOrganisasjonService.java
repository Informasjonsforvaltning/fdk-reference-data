package no.fdk.referencedata.ssb.kommuneorganisasjoner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class KommuneOrganisasjonService {

    private final KommuneOrganisasjonHarvester kommuneOrganisasjonHarvester;
    private final KommuneOrganisasjonRepository kommuneOrganisasjonRepository;

    @Autowired
    public KommuneOrganisasjonService(KommuneOrganisasjonHarvester kommuneOrganisasjonHarvester,
                                      KommuneOrganisasjonRepository kommuneOrganisasjonRepository) {
        this.kommuneOrganisasjonHarvester = kommuneOrganisasjonHarvester;
        this.kommuneOrganisasjonRepository = kommuneOrganisasjonRepository;
    }

    public boolean firstTime() {
        return kommuneOrganisasjonRepository.count() == 0;
    }

    @Transactional
    public void harvestAndSave() {
        try {
            kommuneOrganisasjonRepository.deleteAll();

            final AtomicInteger counter = new AtomicInteger(0);
            final Iterable<KommuneOrganisasjon> iterable = kommuneOrganisasjonHarvester.harvest().toIterable();
            iterable.forEach(item -> counter.getAndIncrement());
            log.info("Harvest and saving {} kommunale organisasjoner", counter.get());
            kommuneOrganisasjonRepository.saveAll(iterable);

        } catch(Exception e) {
            log.error("Unable to harvest kommunale organisasjoner", e);
        }
    }
}
