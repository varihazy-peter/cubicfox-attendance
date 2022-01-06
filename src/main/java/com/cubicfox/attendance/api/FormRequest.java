package com.cubicfox.attendance.api;

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
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class FormRequest {
    @NotBlank
    String name;
    @NotNull
    YearMonth yearMonth;

    Boolean useCalendar;

    boolean useCalendar() {
        return useCalendar == null || useCalendar.booleanValue();
    }

    List<@Valid @Min(1) @Max(31) Integer> h8;
    List<@Valid @Min(1) @Max(31) Integer> fs;
    List<@Valid @Min(1) @Max(31) Integer> bs;
    List<@Valid @Min(1) @Max(31) Integer> lo;

    Map<DayModifier, ? extends Collection<Integer>> placeHolders() {
        return Map.of( //
                DayModifier.FS, fs == null ? List.of() : fs, //
                DayModifier.H8, h8 == null ? List.of() : h8, //
                DayModifier.BS, bs == null ? List.of() : bs, //
                DayModifier.LO, lo == null ? List.of() : lo //
        );
    }

    Map<String, String> params() {
        Map<String, String> map = new HashMap<String, String>();
        addIf(map, "name", name);
        addIf(map, "yearMonth", yearMonth);
        addIf(map, "h8", h8);
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