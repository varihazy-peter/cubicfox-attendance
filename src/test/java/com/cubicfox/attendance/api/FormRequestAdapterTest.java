package com.cubicfox.attendance.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cubicfox.attendance.AttendanceCalendar;
import com.cubicfox.attendance.domain.MonthlyAttendance;
import com.google.common.primitives.Ints;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { FormRequestAdapter.class, AttendanceCalendar.class })
class FormRequestAdapterTest {
    @Autowired
    FormRequestAdapter formRequestAdapter;

    private final String name = "name";
    private final YearMonth ym202110 = YearMonth.of(2021, 10);
    private final int[] ym202110Days = IntStream.rangeClosed(1, ym202110.atEndOfMonth().getDayOfMonth()).toArray();
    FormRequest.FormRequestBuilder base = FormRequest.builder().name(name).yearMonth(ym202110);

    @Test
    void test_conflictingDay() {
        FormRequest request = base.lo(List.of(1)).hours(List.of(1, 2)).build();
        assertThatThrownBy(() -> this.formRequestAdapter.map(request)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void test_empty_withoutCalendar() {
        MonthlyAttendance calculated = this.formRequestAdapter.map(base.noCalendarHelper(true).build());
        assertThat(calculated).isEqualTo(new MonthlyAttendance(name, ym202110, Map.of()));
    }

    @Test
    void test_empty_withCalendar() {
        MonthlyAttendance calculated = this.formRequestAdapter.map(base.noCalendarHelper(false).build());
        assertThat(calculated.getName()).isEqualTo(name);
        assertThat(calculated.getYearMonth()).isEqualTo(ym202110);
        assertThat(calculated.getDays()).containsOnlyKeys(Ints.asList(ym202110Days));
    }

}
