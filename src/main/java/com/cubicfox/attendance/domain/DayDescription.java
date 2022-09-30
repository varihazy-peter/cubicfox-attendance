package com.cubicfox.attendance.domain;

import lombok.Value;

public interface DayDescription {

    public final static DayDescription H8 = of("8", "09:00", "17:30");

    String getStart();

    String getEnd();

    String getText();

    static DayDescription of(String text) {
        return new ImmutableDayDescription(text, null, null);
    }

    static DayDescription of(String start, String end, String text) {
        return new ImmutableDayDescription(text, start, end);
    }

}