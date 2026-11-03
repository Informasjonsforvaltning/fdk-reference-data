package no.fdk.referencedata.lovdata;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LegalResources {
    List<LegalResource> legalResources;
}
