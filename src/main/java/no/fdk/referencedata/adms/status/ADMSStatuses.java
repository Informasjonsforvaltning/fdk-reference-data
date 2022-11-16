package no.fdk.referencedata.adms.status;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ADMSStatuses {
    List<ADMSStatus> statuses;
}
