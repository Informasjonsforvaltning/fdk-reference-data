package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.schema.dayofweek.DayOfWeek;
import no.fdk.referencedata.schema.dayofweek.DayOfWeekService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DayOfWeekQuery {

    private final DayOfWeekService dayOfWeekService;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<DayOfWeek> weekDays() {
        return support.allSortedByUri(dayOfWeekService.getAll(), DayOfWeek::getUri);
    }

    @QueryMapping
    public DayOfWeek dayOfWeekByCode(@Argument String code) {
        return support.byCode(dayOfWeekService::getByCode, code);
    }
}
