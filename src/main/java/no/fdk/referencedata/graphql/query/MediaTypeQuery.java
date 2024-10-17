package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.iana.mediatype.MediaType;
import no.fdk.referencedata.iana.mediatype.MediaTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class MediaTypeQuery {

    @Autowired
    private MediaTypeRepository mediaTypeRepository;

    @QueryMapping
    public List<MediaType> mediaTypes() {
        return StreamSupport.stream(mediaTypeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(MediaType::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public List<MediaType> mediaTypesByType(@Argument String type) {
        return mediaTypeRepository.findByType(type).stream()
                .sorted(Comparator.comparing(MediaType::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public MediaType mediaTypeByTypeAndSubType(@Argument String type, @Argument String subType) {
        return mediaTypeRepository.findByTypeAndSubType(type, subType).orElse(null);
    }
}
