package no.fdk.referencedata.digdir.audiencetype;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AudienceTypes {
    List<AudienceType> audienceTypes;
}
