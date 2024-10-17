package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.digdir.audiencetype.AudienceType;
import no.fdk.referencedata.digdir.audiencetype.AudienceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class AudienceTypeQuery {

    @Autowired
    private AudienceTypeRepository audienceTypeRepository;

    @QueryMapping
    public List<AudienceType> audienceTypes() {
        return StreamSupport.stream(audienceTypeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(AudienceType::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public AudienceType audienceTypeByCode(@Argument String code) {
        return audienceTypeRepository.findByCode(code).orElse(null);
    }
}
