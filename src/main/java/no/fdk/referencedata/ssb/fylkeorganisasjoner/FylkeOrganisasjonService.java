package no.fdk.referencedata.ssb.fylkeorganisasjoner;

import no.fdk.referencedata.core.ReferenceDataWriter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FylkeOrganisasjonService {

    private final FylkeOrganisasjonHarvester fylkeOrganisasjonHarvester;

    private final ReferenceDataWriter referenceDataWriter;
    private final FylkeOrganisasjonRepository fylkeOrganisasjonRepository;

    @Autowired
    public FylkeOrganisasjonService(
            FylkeOrganisasjonHarvester fylkeOrganisasjonHarvester,
            FylkeOrganisasjonRepository fylkeOrganisasjonRepository,
            ReferenceDataWriter referenceDataWriter) {
        this.fylkeOrganisasjonHarvester = fylkeOrganisasjonHarvester;
        this.fylkeOrganisasjonRepository = fylkeOrganisasjonRepository;
        this.referenceDataWriter = referenceDataWriter;
    }

    public boolean firstTime() {
        return fylkeOrganisasjonRepository.count() == 0;
    }

    public void harvestAndSave() {
        try {
            final List<FylkeOrganisasjon> items = new ArrayList<>();
            fylkeOrganisasjonHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} fylkeskommunale organisasjoner", items.size());

            referenceDataWriter.replaceAll(fylkeOrganisasjonRepository, items);

        } catch (Exception e) {
            log.error("Unable to harvest fylkeskommunale organisasjoner", e);
        }
    }
}
