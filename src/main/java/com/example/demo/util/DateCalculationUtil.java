package com.example.demo.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

public final class DateCalculationUtil {


    private DateCalculationUtil() {
    }

    /**
     * Calculates the number of days between two dates, excluding specified days of the week.
     * The calculation is inclusive of the end date.
     *
     * @param startDate The start date (e.g., the day after the due date).
     * @param endDate   The end date (e.g., the return date).
     * @return The count of valid days.
     */
    public static long calculateWorkingDays(LocalDate startDate, LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            return 0;
        }

        Set<DayOfWeek> daysToSkip = Set.of(DayOfWeek.MONDAY, DayOfWeek.SUNDAY);

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate.plusDays(1));

        long skippedDays = startDate.datesUntil(endDate.plusDays(1)).filter(date -> daysToSkip.contains(date.getDayOfWeek())).count();

        return totalDays - skippedDays;
    }
}