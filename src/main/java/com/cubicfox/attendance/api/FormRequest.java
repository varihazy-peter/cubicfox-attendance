package com.cubicfox.attendance.api;

import com.cubicfox.attendance.WorkCalendar;
import com.cubicfox.attendance.domain.DayDescription;
import com.cubicfox.attendance.domain.DayModifier;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class FormRequest {
    @NotBlank
    String name;
    @NotNull
    YearMonth yearMonth;

    WorkCalendar workCalendar;

    List<@Valid @Min(1) @Max(31) Integer> hours;
    List<@Valid @Min(1) @Max(31) Integer> fs;
    List<@Valid @Min(1) @Max(31) Integer> bs;
    List<@Valid @Min(1) @Max(31) Integer> lo;

    @Size(max = 0, message = "Conflicting days {value}")
    public List<Integer> getConflictingDays() {
        return Stream.of(hours, fs, bs, lo) //
                .filter(Objects::nonNull) //
                .flatMap(List::stream) //
                .filter(Objects::nonNull) //
                .collect(Collectors.groupingBy(d -> d, Collectors.counting())) //
                .entrySet() //
                .stream() //
                .filter(e -> e.getValue().longValue() != 1l) //
                .map(Map.Entry::getKey) //
                .collect(Collectors.toUnmodifiableList());
    }

    Map<DayDescription, List<Integer>> placeHolders() {
        return Map.of( //
                DayModifier.FS, fs == null ? List.of() : fs, //
                workCalendar == null ? DayDescription.H8 : workCalendar.getDayDescription(),
                hours == null ? List.of() : hours, //
                DayModifier.BS, bs == null ? List.of() : bs, //
                DayModifier.LO, lo == null ? List.of() : lo //
        );
    }
}