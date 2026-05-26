package no.fdk.referencedata.ssb.fylkeorganisasjoner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class FylkeOrganisasjonWriter {

    private final FylkeOrganisasjonRepository fylkeOrganisasjonRepository;

    @Autowired
    public FylkeOrganisasjonWriter(FylkeOrganisasjonRepository fylkeOrganisasjonRepository) {
        this.fylkeOrganisasjonRepository = fylkeOrganisasjonRepository;
    }

    @Transactional
    public void replaceAll(List<FylkeOrganisasjon> items) {
        fylkeOrganisasjonRepository.deleteAll();
        fylkeOrganisasjonRepository.saveAll(items);
    }
}
