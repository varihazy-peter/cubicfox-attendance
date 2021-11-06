package com.cubicfox.attendance.imagemaker;

import io.micrometer.core.lang.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlaceHolder {
    LO(null), // special, no text representation; indicates the day should leave out
    FS, BS, H8("8");

    private PlaceHolder() {
        this.text = this.name();
    }

    @Nullable
    private final String text;

    public boolean display() {
        return text != null;
    }
}
