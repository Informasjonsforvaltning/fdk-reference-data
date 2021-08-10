package no.fdk.referencedata.mediatype;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MediaTypes {
    List<MediaType> mediaTypes;
}
