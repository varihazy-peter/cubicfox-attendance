package com.cubicfox.attendance.domain;

import java.time.YearMonth;
import java.util.Map;
import lombok.Value;

@Value
public class MonthlyAttendance {

    String name;
    YearMonth yearMonth;
    Map<Integer, DayDescription> days;

    public MonthlyAttendance(String name, YearMonth yearMonth, Map<Integer, DayDescription> days) {
        this.name = name;
        this.yearMonth = yearMonth;
        this.days = Map.copyOf(days);
    }
}
