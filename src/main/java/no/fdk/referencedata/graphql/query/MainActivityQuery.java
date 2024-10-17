package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.eu.mainactivity.MainActivity;
import no.fdk.referencedata.eu.mainactivity.MainActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class MainActivityQuery {

    @Autowired
    private MainActivityRepository mainActivityRepository;

    public List<MainActivity> getMainActivities() {
        return StreamSupport.stream(mainActivityRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(MainActivity::getUri))
                .collect(Collectors.toList());
    }

    public MainActivity getMainActivityByCode(@Argument String code) {
        return mainActivityRepository.findByCode(code).orElse(null);
    }
}
