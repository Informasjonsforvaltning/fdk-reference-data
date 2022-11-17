package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.eu.mainactivity.MainActivity;
import no.fdk.referencedata.eu.mainactivity.MainActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class MainActivityQuery implements GraphQLQueryResolver {

    @Autowired
    private MainActivityRepository mainActivityRepository;

    public List<MainActivity> getMainActivities() {
        return StreamSupport.stream(mainActivityRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(MainActivity::getUri))
                .collect(Collectors.toList());
    }

    public MainActivity getMainActivityByCode(String code) {
        return mainActivityRepository.findByCode(code).orElse(null);
    }
}
