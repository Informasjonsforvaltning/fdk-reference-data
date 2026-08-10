package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.linguisticsystem.LinguisticSystem;
import no.fdk.referencedata.linguisticsystem.LinguisticSystemService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LinguisticSystemQuery {

    private final LinguisticSystemService linguisticSystemService;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<LinguisticSystem> linguisticSystems() {
        return support.allSortedByUri(linguisticSystemService.getAll(), LinguisticSystem::getUri);
    }

    @QueryMapping
    public LinguisticSystem linguisticSystemByCode(@Argument String code) {
        return support.byCode(linguisticSystemService::getByCode, code);
    }
}
