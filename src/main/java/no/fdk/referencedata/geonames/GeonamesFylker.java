package no.fdk.referencedata.geonames;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GeonamesFylker {
    List<GeonamesFylke> fylker;
}
