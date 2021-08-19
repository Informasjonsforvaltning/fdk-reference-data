package no.fdk.referencedata.iana.mediatype;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MediaTypes {
    List<MediaType> mediaTypes;
}
