package no.fdk.referencedata.mediatype;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document
public class MediaType {
    @Id
    String uri;
    String name;
    @Indexed
    String type;
    @Indexed
    String subType;
}
