package no.fdk.referencedata.adms.publishertype;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PublisherTypes {
    List<PublisherType> publisherTypes;
}
