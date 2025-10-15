package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.mobility.datastandard.MobilityDataStandard;
import no.fdk.referencedata.mobility.datastandard.MobilityDataStandardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class MobilityDataStandardQuery {
    private final MobilityDataStandardRepository mobilityDataStandardRepository;

    @Autowired
    public MobilityDataStandardQuery(MobilityDataStandardRepository mobilityDataStandardRepository) {
        this.mobilityDataStandardRepository = mobilityDataStandardRepository;
    }

    @QueryMapping
    public List<MobilityDataStandard> mobilityDataStandards() {
        return StreamSupport.stream(mobilityDataStandardRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(MobilityDataStandard::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public MobilityDataStandard mobilityDataStandardByCode(@Argument String code) {
        return mobilityDataStandardRepository.findByCode(code).orElse(null);
    }
}
