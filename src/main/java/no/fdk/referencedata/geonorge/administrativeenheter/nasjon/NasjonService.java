package no.fdk.referencedata.geonorge.administrativeenheter.nasjon;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.Kommune;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneHarvester;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneRepository;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class NasjonService {
    private final static Nasjon NORGE = Nasjon.builder()
            .uri("https://data.geonorge.no/administrativeEnheter/nasjon/id/173163")
            .nasjonsnavn("Norge")
            .nasjonsnummer("173163")
            .build();

    public List<Nasjon> getNasjoner() {
        return List.of(NORGE);
    }

    public Optional<Nasjon> getNasjonByNasjonsnummer(String nasjonsnummer) {
        return getNasjoner().stream()
                .filter(nasjon -> nasjon.getNasjonsnummer().equals(nasjonsnummer))
                .findFirst();
    }
}
