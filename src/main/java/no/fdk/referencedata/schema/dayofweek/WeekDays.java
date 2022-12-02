package no.fdk.referencedata.schema.dayofweek;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WeekDays {
    List<DayOfWeek> weekDays;
}
