package no.fdk.referencedata.mediatype;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@Builder
@RedisHash("MediaType")
public class MediaType {
    @Id
    String uri;
    String name;
    String type;
    String subType;
}