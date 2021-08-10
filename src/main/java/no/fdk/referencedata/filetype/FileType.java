package no.fdk.referencedata.filetype;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document
public class FileType {
    @Id
    String uri;
    @Indexed
    String code;
    String mediaType;
}
