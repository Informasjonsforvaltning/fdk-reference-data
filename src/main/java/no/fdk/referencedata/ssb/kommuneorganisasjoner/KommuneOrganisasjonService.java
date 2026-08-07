package no.fdk.referencedata.ssb.kommuneorganisasjoner;

import no.fdk.referencedata.core.ReferenceDataWriter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class KommuneOrganisasjonService {

    private final KommuneOrganisasjonHarvester kommuneOrganisasjonHarvester;

    private final ReferenceDataWriter referenceDataWriter;
    private final KommuneOrganisasjonRepository kommuneOrganisasjonRepository;

    @Autowired
    public KommuneOrganisasjonService(
            KommuneOrganisasjonHarvester kommuneOrganisasjonHarvester,
            KommuneOrganisasjonRepository kommuneOrganisasjonRepository,
            ReferenceDataWriter referenceDataWriter) {
        this.kommuneOrganisasjonHarvester = kommuneOrganisasjonHarvester;
        this.kommuneOrganisasjonRepository = kommuneOrganisasjonRepository;
        this.referenceDataWriter = referenceDataWriter;
    }

    public boolean firstTime() {
        return kommuneOrganisasjonRepository.count() == 0;
    }

    public void harvestAndSave() {
        try {
            final List<KommuneOrganisasjon> items = new ArrayList<>();
            kommuneOrganisasjonHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} kommunale organisasjoner", items.size());

            referenceDataWriter.replaceAll(kommuneOrganisasjonRepository, items);

        } catch (Exception e) {
            log.error("Unable to harvest kommunale organisasjoner", e);
        }
    }
}
