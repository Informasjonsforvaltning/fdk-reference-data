package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.schema.dayofweek.DayOfWeek;
import no.fdk.referencedata.schema.dayofweek.DayOfWeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DayOfWeekQuery implements GraphQLQueryResolver {

    private final DayOfWeekService dayOfWeekService;

    @Autowired
    public DayOfWeekQuery(DayOfWeekService dayOfWeekService) {
        this.dayOfWeekService = dayOfWeekService;
    }

    public List<DayOfWeek> getWeekDays() {
        return dayOfWeekService.getAll().stream()
                .sorted(Comparator.comparing(DayOfWeek::getUri))
                .collect(Collectors.toList());
    }

    public DayOfWeek getDayOfWeekByCode(String code) {
        return dayOfWeekService.getByCode(code).orElse(null);
    }
}
