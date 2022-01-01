package com.cubicfox.attendance.domain;

import java.time.YearMonth;
import java.util.Map;
import lombok.Value;

@Value
public class MonthlyAttendance {

    String name;
    YearMonth yearMonth;
    Map<Integer, DayModifier> days;

    public MonthlyAttendance(String name, YearMonth yearMonth, Map<Integer, DayModifier> days) {
        this.name = name;
        this.yearMonth = yearMonth;
        this.days = Map.copyOf(days);
    }
}
