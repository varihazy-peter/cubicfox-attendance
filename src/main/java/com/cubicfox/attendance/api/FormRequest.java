package com.cubicfox.attendance.api;

import com.cubicfox.attendance.domain.DayDescription;
import com.cubicfox.attendance.domain.DayModifier;
import java.time.YearMonth;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
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

    Boolean noCalendarHelper;

    boolean useCalendar() {
        return noCalendarHelper == null || !noCalendarHelper.booleanValue();
    }

    List<@Valid @Min(1) @Max(31) Integer> hours;
    List<@Valid @Min(1) @Max(31) Integer> fs;
    List<@Valid @Min(1) @Max(31) Integer> bs;
    List<@Valid @Min(1) @Max(31) Integer> lo;

    @Size(max = 0, message = "Conflicting days {value}")
    public List<Integer> getConflictingDays() {
        return placeHolders().values().stream().flatMap(Collection::stream)
                .collect(Collectors.groupingBy(d -> d, Collectors.counting()))
                // map<day,count>
                .entrySet().stream().filter(e -> e.getValue().longValue() != 1l).map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableList());
    }

    Map<DayDescription, ? extends Collection<Integer>> placeHolders() {
        return Map.of( //
                DayModifier.FS, fs == null ? List.of() : fs, //
                DayDescription.H9_5, hours == null ? List.of() : hours, //
                DayModifier.BS, bs == null ? List.of() : bs, //
                DayModifier.LO, lo == null ? List.of() : lo //
        );
    }

    Map<String, String> params() {
        Map<String, String> map = new HashMap<String, String>();
        addIf(map, "name", name);
        addIf(map, "yearMonth", yearMonth);
        addIf(map, "hours", hours);
        addIf(map, "fs", fs);
        addIf(map, "bs", bs);
        addIf(map, "lo", lo);
        return map;
    }

    private void addIf(Map<String, String> map, String key, Object object) {
        if (object == null) {
            return;
        }
        if (object instanceof Collection<?>) {
            Collection<?> c = (Collection<?>) object;
            if (!c.isEmpty()) {
                map.put(key, convert(c));
            }
        } else
            map.put(key, String.valueOf(object));
    }

    private String convert(Collection<?> c) {
        return c.stream().filter(Objects::nonNull).map(String::valueOf).collect(Collectors.joining(","));
    }

}