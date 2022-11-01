package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.iana.mediatype.MediaType;
import no.fdk.referencedata.iana.mediatype.MediaTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class MediaTypeQuery implements GraphQLQueryResolver {

    @Autowired
    private MediaTypeRepository mediaTypeRepository;

    public List<MediaType> getMediaTypes() {
        return StreamSupport.stream(mediaTypeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(MediaType::getUri))
                .collect(Collectors.toList());
    }

    public List<MediaType> getMediaTypesByType(String type) {
        return mediaTypeRepository.findByType(type).stream()
                .sorted(Comparator.comparing(MediaType::getUri))
                .collect(Collectors.toList());
    }

    public MediaType getMediaTypeByTypeAndSubType(String type, String subType) {
        return mediaTypeRepository.findByTypeAndSubType(type, subType).orElse(null);
    }
}