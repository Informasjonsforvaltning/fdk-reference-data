package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.schema.dayofweek.DayOfWeek;
import no.fdk.referencedata.schema.dayofweek.DayOfWeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DayOfWeekQuery {

    private final DayOfWeekService dayOfWeekService;

    @Autowired
    public DayOfWeekQuery(DayOfWeekService dayOfWeekService) {
        this.dayOfWeekService = dayOfWeekService;
    }

    @QueryMapping
    public List<DayOfWeek> weekDays() {
        return dayOfWeekService.getAll().stream()
                .sorted(Comparator.comparing(DayOfWeek::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public DayOfWeek dayOfWeekByCode(@Argument String code) {
        return dayOfWeekService.getByCode(code).orElse(null);
    }
}
