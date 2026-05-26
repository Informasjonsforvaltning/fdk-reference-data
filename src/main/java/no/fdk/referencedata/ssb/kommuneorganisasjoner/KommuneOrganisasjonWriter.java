package no.fdk.referencedata.ssb.kommuneorganisasjoner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class KommuneOrganisasjonWriter {

    private final KommuneOrganisasjonRepository kommuneOrganisasjonRepository;

    @Autowired
    public KommuneOrganisasjonWriter(KommuneOrganisasjonRepository kommuneOrganisasjonRepository) {
        this.kommuneOrganisasjonRepository = kommuneOrganisasjonRepository;
    }

    @Transactional
    public void replaceAll(List<KommuneOrganisasjon> items) {
        kommuneOrganisasjonRepository.deleteAll();
        kommuneOrganisasjonRepository.saveAll(items);
    }
}
