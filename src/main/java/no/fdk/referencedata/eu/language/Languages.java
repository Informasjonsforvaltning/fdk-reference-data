package no.fdk.referencedata.eu.language;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Languages {
    List<Language> languages;
}
