package no.fdk.referencedata.filetype;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@Builder
@RedisHash("FileType")
public class FileType {
    @Id
    String uri;
    String code;
    String mediaType;
}