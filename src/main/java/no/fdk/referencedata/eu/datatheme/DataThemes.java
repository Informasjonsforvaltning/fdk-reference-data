package no.fdk.referencedata.eu.datatheme;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DataThemes {
    List<DataTheme> dataThemes;
}
