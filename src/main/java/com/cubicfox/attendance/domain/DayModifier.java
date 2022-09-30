package com.cubicfox.attendance.domain;

import io.micrometer.core.lang.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DayModifier implements DayDescription {
    LO(null), // special, no text representation; indicates the day should leave out
    FS, BS;

    private DayModifier() {
        this.text = this.name();
    }

    @Nullable
    private final String text;

    public boolean display() {
        return text != null;
    }

    @Override
    public String getStart() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getEnd() {
        // TODO Auto-generated method stub
        return null;
    }
}