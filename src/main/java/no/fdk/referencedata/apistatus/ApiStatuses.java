package no.fdk.referencedata.apistatus;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ApiStatuses {
    List<ApiStatus> apiStatuses;
}
