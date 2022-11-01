package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.linguisticsystem.LinguisticSystem;
import no.fdk.referencedata.linguisticsystem.LinguisticSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LinguisticSystemQuery implements GraphQLQueryResolver {

    private final LinguisticSystemService linguisticSystemService;

    @Autowired
    public LinguisticSystemQuery(LinguisticSystemService linguisticSystemService) {
        this.linguisticSystemService = linguisticSystemService;
    }

    public List<LinguisticSystem> getLinguisticSystems() {
        return linguisticSystemService.getAll().stream()
                .sorted(Comparator.comparing(LinguisticSystem::getUri))
                .collect(Collectors.toList());
    }

    public LinguisticSystem getLinguisticSystemByCode(String code) {
        return linguisticSystemService.getByCode(code).orElse(null);
    }
}
