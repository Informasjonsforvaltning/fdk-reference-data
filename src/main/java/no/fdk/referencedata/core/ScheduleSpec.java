package no.fdk.referencedata.core;

public record ScheduleSpec(String cron) {

    public static ScheduleSpec of(String cron) {
        return new ScheduleSpec(cron);
    }
}
