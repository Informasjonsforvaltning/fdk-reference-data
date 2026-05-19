package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.geonames.GeonamesFylke;
import no.fdk.referencedata.geonames.GeonamesFylkeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class GeonamesFylkeQuery {

    @Autowired
    private GeonamesFylkeRepository geonamesFylkeRepository;

    @QueryMapping
    public List<GeonamesFylke> geonamesFylker() {
        return StreamSupport.stream(geonamesFylkeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(GeonamesFylke::getName))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public GeonamesFylke geonamesFylkeByGeonameId(@Argument String geonameId) {
        return geonamesFylkeRepository.findByGeonameId(geonameId).orElse(null);
    }
}
