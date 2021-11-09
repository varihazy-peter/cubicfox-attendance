package com.cubicfox.attendance.imagemaker;

import java.time.YearMonth;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
public class AttendanceRequestDTO {
    String name;
    YearMonth yearMonth;
    Map<Integer, PlaceHolder> placeHolders;

    @Builder(builderMethodName = "builderFrom")
    public static AttendanceRequestDTO from(@NonNull String name, @NonNull YearMonth yearMonth,
            @NonNull Map<PlaceHolder, ? extends Collection<Integer>> placeHolders) {
        Map<Integer, Set<PlaceHolder>> map = placeHolders.entrySet().stream()
                .flatMap(e -> e.getValue().stream().map(v -> Map.entry(v, e.getKey()))).collect(Collectors.groupingBy(
                        Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toUnmodifiableSet())));
        Map<Integer, Set<PlaceHolder>> errors = map.entrySet().stream().filter(e -> e.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Conflicting Placeholders " + errors);
        }
        return new AttendanceRequestDTO(name, yearMonth, map.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> e.getValue().iterator().next())));
    }

}
