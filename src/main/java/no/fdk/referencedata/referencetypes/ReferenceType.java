package no.fdk.referencedata.referencetypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceType {
    String code;
    Map<String, String> label;
    Map<String, String> inverseLabel;
}
