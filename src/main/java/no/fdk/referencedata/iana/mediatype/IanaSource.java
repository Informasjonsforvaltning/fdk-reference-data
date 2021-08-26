package no.fdk.referencedata.iana.mediatype;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.io.Resource;

@Data
@AllArgsConstructor
public class IanaSource {
    String type;
    Resource resource;
}
