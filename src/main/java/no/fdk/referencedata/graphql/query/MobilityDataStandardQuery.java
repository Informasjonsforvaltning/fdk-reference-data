package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.mobility.datastandard.MobilityDataStandard;
import no.fdk.referencedata.mobility.datastandard.MobilityDataStandardRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MobilityDataStandardQuery {

    private final MobilityDataStandardRepository mobilityDataStandardRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<MobilityDataStandard> mobilityDataStandards() {
        return support.allSortedByUri(mobilityDataStandardRepository, MobilityDataStandard::getUri);
    }

    @QueryMapping
    public MobilityDataStandard mobilityDataStandardByCode(@Argument String code) {
        return support.byCode(mobilityDataStandardRepository::findByCode, code);
    }
}
