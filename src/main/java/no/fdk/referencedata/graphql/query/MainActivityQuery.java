package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.eu.mainactivity.MainActivity;
import no.fdk.referencedata.eu.mainactivity.MainActivityRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainActivityQuery {

    private final MainActivityRepository mainActivityRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<MainActivity> mainActivities() {
        return support.allSortedByUri(mainActivityRepository, MainActivity::getUri);
    }

    @QueryMapping
    public MainActivity mainActivityByCode(@Argument String code) {
        return support.byCode(mainActivityRepository::findByCode, code);
    }
}
