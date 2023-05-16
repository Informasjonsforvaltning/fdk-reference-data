package no.fdk.referencedata.ssb.fylkeorganisasjoner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class FylkeOrganisasjonService {

    private final FylkeOrganisasjonHarvester fylkeOrganisasjonHarvester;
    private final FylkeOrganisasjonRepository fylkeOrganisasjonRepository;

    @Autowired
    public FylkeOrganisasjonService(FylkeOrganisasjonHarvester fylkeOrganisasjonHarvester,
                                    FylkeOrganisasjonRepository fylkeOrganisasjonRepository) {
        this.fylkeOrganisasjonHarvester = fylkeOrganisasjonHarvester;
        this.fylkeOrganisasjonRepository = fylkeOrganisasjonRepository;
    }

    public boolean firstTime() {
        return fylkeOrganisasjonRepository.count() == 0;
    }

    @Transactional
    public void harvestAndSave() {
        try {
            fylkeOrganisasjonRepository.deleteAll();

            final AtomicInteger counter = new AtomicInteger(0);
            final Iterable<FylkeOrganisasjon> iterable = fylkeOrganisasjonHarvester.harvest().toIterable();
            iterable.forEach(item -> counter.getAndIncrement());
            log.info("Harvest and saving {} fylkeskommunale organisasjoner", counter.get());
            fylkeOrganisasjonRepository.saveAll(iterable);

        } catch(Exception e) {
            log.error("Unable to harvest fylkeskommunale organisasjoner", e);
        }
    }
}
