package no.fdk.referencedata.mobility.theme;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MobilityThemes {
    List<MobilityTheme> mobilityThemes;
}
