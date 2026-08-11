package no.fdk.referencedata.ssb.kommuneorganisasjoner;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KommuneOrganisasjonService implements HarvestableReferenceData {

    private final KommuneOrganisasjonHarvester kommuneOrganisasjonHarvester;
    private final KommuneOrganisasjonRepository kommuneOrganisasjonRepository;
    private final ReferenceDataServiceSupport support;

    @Autowired
    public KommuneOrganisasjonService(
            KommuneOrganisasjonHarvester kommuneOrganisasjonHarvester,
            KommuneOrganisasjonRepository kommuneOrganisasjonRepository,
            ReferenceDataServiceSupport support) {
        this.kommuneOrganisasjonHarvester = kommuneOrganisasjonHarvester;
        this.kommuneOrganisasjonRepository = kommuneOrganisasjonRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(kommuneOrganisasjonRepository);
    }

    @Override
    public HarvestResult harvestAndSave() {
        return support.harvestAndSaveWithoutRdf(
                kommuneOrganisasjonHarvester::harvest,
                kommuneOrganisasjonRepository,
                "kommune-organisasjon");
    }
}
