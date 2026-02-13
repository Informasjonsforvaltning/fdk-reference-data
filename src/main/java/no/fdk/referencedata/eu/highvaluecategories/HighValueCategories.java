package no.fdk.referencedata.eu.highvaluecategories;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HighValueCategories {
    List<HighValueCategory> highValueCategories;
}
