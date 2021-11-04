package com.cubicfox.attendance.imagemaker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlaceHolder {
    FS, BS, LO("");

    private PlaceHolder() {
        text = this.name();
    }

    private final String text;
}
