package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.eu.frequency.Frequency;
import no.fdk.referencedata.eu.frequency.FrequencyRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class FrequencyQuery {

    private final FrequencyRepository frequencyRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<Frequency> frequencies() {
        return support.allSortedByUri(frequencyRepository, Frequency::getUri);
    }

    @QueryMapping
    public Frequency frequencyByCode(@Argument String code) {
        return support.byCode(frequencyRepository::findByCode, code);
    }
}
