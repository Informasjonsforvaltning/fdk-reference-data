package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.linguisticsystem.LinguisticSystem;
import no.fdk.referencedata.linguisticsystem.LinguisticSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class LinguisticSystemQuery {

    private final LinguisticSystemService linguisticSystemService;

    @Autowired
    public LinguisticSystemQuery(LinguisticSystemService linguisticSystemService) {
        this.linguisticSystemService = linguisticSystemService;
    }

    @QueryMapping
    public List<LinguisticSystem> linguisticSystems() {
        return linguisticSystemService.getAll().stream()
                .sorted(Comparator.comparing(LinguisticSystem::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public LinguisticSystem linguisticSystemByCode(@Argument String code) {
        return linguisticSystemService.getByCode(code).orElse(null);
    }
}
