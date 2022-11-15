package no.fdk.referencedata.eu.mainactivity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MainActivities {
    List<MainActivity> mainActivities;
}
