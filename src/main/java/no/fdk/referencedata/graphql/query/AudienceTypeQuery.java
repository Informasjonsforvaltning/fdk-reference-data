package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.digdir.audiencetype.AudienceType;
import no.fdk.referencedata.digdir.audiencetype.AudienceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class AudienceTypeQuery implements GraphQLQueryResolver {

    @Autowired
    private AudienceTypeRepository audienceTypeRepository;

    public List<AudienceType> getAudienceTypes() {
        return StreamSupport.stream(audienceTypeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(AudienceType::getUri))
                .collect(Collectors.toList());
    }

    public AudienceType getAudienceTypeByCode(String code) {
        return audienceTypeRepository.findByCode(code).orElse(null);
    }
}
