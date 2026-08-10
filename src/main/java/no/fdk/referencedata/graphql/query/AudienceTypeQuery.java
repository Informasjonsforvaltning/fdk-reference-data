package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.digdir.audiencetype.AudienceType;
import no.fdk.referencedata.digdir.audiencetype.AudienceTypeRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AudienceTypeQuery {

    private final AudienceTypeRepository audienceTypeRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<AudienceType> audienceTypes() {
        return support.allSortedByUri(audienceTypeRepository, AudienceType::getUri);
    }

    @QueryMapping
    public AudienceType audienceTypeByCode(@Argument String code) {
        return support.byCode(audienceTypeRepository::findByCode, code);
    }
}
