package no.fdk.referencedata.ssb.fylkeorganisasjoner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FylkeOrganisasjonService {

    private final FylkeOrganisasjonHarvester fylkeOrganisasjonHarvester;

    private final FylkeOrganisasjonWriter fylkeOrganisasjonWriter;
    private final FylkeOrganisasjonRepository fylkeOrganisasjonRepository;

    @Autowired
    public FylkeOrganisasjonService(
            FylkeOrganisasjonHarvester fylkeOrganisasjonHarvester,
            FylkeOrganisasjonRepository fylkeOrganisasjonRepository,
            FylkeOrganisasjonWriter fylkeOrganisasjonWriter) {
        this.fylkeOrganisasjonHarvester = fylkeOrganisasjonHarvester;
        this.fylkeOrganisasjonRepository = fylkeOrganisasjonRepository;
        this.fylkeOrganisasjonWriter = fylkeOrganisasjonWriter;
    }

    public boolean firstTime() {
        return fylkeOrganisasjonRepository.count() == 0;
    }

    public void harvestAndSave() {
        try {
            final List<FylkeOrganisasjon> items = new ArrayList<>();
            fylkeOrganisasjonHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} fylkeskommunale organisasjoner", items.size());

            fylkeOrganisasjonWriter.replaceAll(items);

        } catch (Exception e) {
            log.error("Unable to harvest fylkeskommunale organisasjoner", e);
        }
    }
}
