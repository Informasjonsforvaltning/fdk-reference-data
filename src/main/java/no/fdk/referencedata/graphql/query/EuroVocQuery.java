package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.eu.eurovoc.EuroVoc;
import no.fdk.referencedata.eu.eurovoc.EuroVocRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EuroVocQuery {

    private final EuroVocRepository euroVocRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<EuroVoc> euroVocs() {
        return support.allSortedByUri(euroVocRepository, EuroVoc::getUri);
    }

    @QueryMapping
    public EuroVoc euroVocByCode(@Argument String code) {
        return support.byCode(euroVocRepository::findByCode, code);
    }
}
