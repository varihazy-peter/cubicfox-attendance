package com.cubicfox.attendance.domain;

import io.micrometer.core.lang.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DayModifier {
    LO(null), // special, no text representation; indicates the day should leave out
    FS, BS, H8("8");

    private DayModifier() {
        this.text = this.name();
    }

    @Nullable
    private final String text;

    public boolean display() {
        return text != null;
    }
}