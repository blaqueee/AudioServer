package com.axelor.apps.audio.utils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CronExpressionGenerator {

    /**
     *
     * for the case where oneTimeTask = TRUE
     *
     */
    public static String generateOneTimeCronExpression(LocalDateTime dateTime) {
        if (dateTime == null) return null;

        return String.format("%d %d %d %d %d ? %d",
                dateTime.getSecond(),
                dateTime.getMinute(),
                dateTime.getHour(),
                dateTime.getDayOfMonth(),
                dateTime.getMonthValue(),
                dateTime.getYear()
        );
    }

    /**
     *
     * for the case where oneTimeTask = FALSE
     *
     */
    public static String generatePeriodicCronExpression(LocalTime time, String daysOfWeekStr) {
        if (time == null || daysOfWeekStr == null || daysOfWeekStr.isBlank()) return null;

        Set<DayOfWeek> daysOfWeek = generateDaysOfWeek(daysOfWeekStr);

        String minute = String.valueOf(time.getMinute());
        String hour = String.valueOf(time.getHour());

        String daysOfWeekString = daysOfWeek.stream()
                .map(day -> day.name().substring(0, 3))
                .collect(Collectors.joining(","));

        return String.format("0 %s %s ? * %s",
                minute,
                hour,
                daysOfWeekString
        );
    }

    private static Set<DayOfWeek> generateDaysOfWeek(String daysOfWeek) {
        String[] split = daysOfWeek.split(",");
        Set<DayOfWeek> daysOfWeekSet = new HashSet<>();

        for (String day : split) {
            int intValue = Integer.parseInt(day);
            daysOfWeekSet.add(DayOfWeek.of(intValue));
        }

        return daysOfWeekSet;
    }

}
