package com.cubicfox.attendance.imagemaker;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AttendanceRequestDTOTest {

    @Test
    void testFrom() {
        assertThatThrownBy(() -> AttendanceRequestDTO.from("name", YearMonth.of(2021, 1), List.of(),
                Map.of(PlaceHolder.FS, List.of(4), PlaceHolder.BS, List.of(4)))).isInstanceOf(RuntimeException.class);

    }

}
