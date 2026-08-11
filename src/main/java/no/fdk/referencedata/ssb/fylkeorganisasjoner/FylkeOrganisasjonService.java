package no.fdk.referencedata.ssb.fylkeorganisasjoner;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FylkeOrganisasjonService implements HarvestableReferenceData {

    private final FylkeOrganisasjonHarvester fylkeOrganisasjonHarvester;
    private final FylkeOrganisasjonRepository fylkeOrganisasjonRepository;
    private final ReferenceDataServiceSupport support;

    @Autowired
    public FylkeOrganisasjonService(
            FylkeOrganisasjonHarvester fylkeOrganisasjonHarvester,
            FylkeOrganisasjonRepository fylkeOrganisasjonRepository,
            ReferenceDataServiceSupport support) {
        this.fylkeOrganisasjonHarvester = fylkeOrganisasjonHarvester;
        this.fylkeOrganisasjonRepository = fylkeOrganisasjonRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(fylkeOrganisasjonRepository);
    }

    @Override
    public HarvestResult harvestAndSave() {
        return support.harvestAndSaveWithoutRdf(
                fylkeOrganisasjonHarvester::harvest,
                fylkeOrganisasjonRepository,
                "fylke-organisasjon");
    }
}
