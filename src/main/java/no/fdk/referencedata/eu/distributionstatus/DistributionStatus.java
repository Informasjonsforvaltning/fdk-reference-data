package no.fdk.referencedata.eu.distributionstatus;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@Document
public class DistributionStatus {
    @Id
    String uri;
    @Indexed
    String code;
    Map<String, String> label;
    LocalDate startUse;
}
