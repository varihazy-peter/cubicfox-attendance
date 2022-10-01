package com.cubicfox.attendance;

import com.cubicfox.attendance.domain.DayDescription;
import java.time.LocalDate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum WorkCalendar {
    D5H8(5, DayDescription.H8), //
    D4H9(4, DayDescription.H9), //
    D4H9_5(4, DayDescription.H9_5), //
    ;

    private final int days;
    @Getter
    private final DayDescription dayDescription;

    public boolean isWorkDay(LocalDate date) {
        return date.getDayOfWeek().getValue() <= days;
    }
}
