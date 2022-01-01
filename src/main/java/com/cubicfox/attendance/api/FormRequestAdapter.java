package com.cubicfox.attendance.api;

import com.cubicfox.attendance.AttendanceCalendar;
import com.cubicfox.attendance.domain.DayModifier;
import com.cubicfox.attendance.domain.MonthlyAttendance;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
class FormRequestAdapter {
    private final AttendanceCalendar attendanceCalendar;

    public MonthlyAttendance map(FormRequest request) {
        Map<Integer, DayModifier> days = request.useCalendar()
                ? attendanceCalendar.calculateDaysModifiers(request.getYearMonth(), this.daysOf(request))
                : this.daysOf(request);
        return new MonthlyAttendance(request.getName(), request.getYearMonth(), days);
    }

    private Map<Integer, DayModifier> daysOf(FormRequest request) {
        return reduce(checkConflictingDays(groupByDay(request.placeHolders())));
    }

    private final Collector<Map.Entry<Integer, DayModifier>, ?, Map<Integer, Set<DayModifier>>> toDaySet = Collectors
            .groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toUnmodifiableSet()));

    private Map<Integer, Set<DayModifier>> groupByDay(Map<DayModifier, ? extends Collection<Integer>> modifiersDays) {
        return modifiersDays.entrySet().stream().flatMap(e -> e.getValue().stream().map(v -> Map.entry(v, e.getKey())))
                .collect(toDaySet);
    }

    private Map<Integer, Set<DayModifier>> checkConflictingDays(Map<Integer, Set<DayModifier>> map) {
        Map<Integer, Set<DayModifier>> errors = map.entrySet().stream().filter(e -> e.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Conflicting DayModifiers " + errors);
        }
        return map;
    }

    private Map<Integer, DayModifier> reduce(Map<Integer, Set<DayModifier>> map) {
        return map.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> e.getValue().iterator().next()));
    }
}
