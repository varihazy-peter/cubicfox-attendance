package com.cubicfox.attendance.domain;

import lombok.Value;

@Value
class ImmutableDayDescription implements DayDescription {
    String text, start, end;
}
