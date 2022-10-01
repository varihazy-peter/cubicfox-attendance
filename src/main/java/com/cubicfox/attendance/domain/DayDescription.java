package com.cubicfox.attendance.domain;

public interface DayDescription {

    public final static DayDescription H8 = of("8", "09:00", "17:30");
    public final static DayDescription H9 = of("9", "09:00", "18:30");
    public final static DayDescription H9_5 = of("9.5", "09:00", "19:00");

    String getStart();

    String getEnd();

    String getText();

    static DayDescription of(String text) {
        return new ImmutableDayDescription(text, null, null);
    }

    static DayDescription of(String text, String start, String end) {
        return new ImmutableDayDescription(text, start, end);
    }

}