package com.cubicfox.attendance.api;

import com.cubicfox.attendance.DayCalculator;
import com.cubicfox.attendance.domain.DayDescription;
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
    private final DayCalculator dayCalculator;

    public MonthlyAttendance map(FormRequest request) {
        Map<Integer, DayDescription> days = request.useCalendar()
                ? dayCalculator.calculateDaysModifiers(request.getYearMonth(), this.daysOf(request))
                : this.daysOf(request);
        return new MonthlyAttendance(request.getName(), request.getYearMonth(), days);
    }

    private Map<Integer, DayDescription> daysOf(FormRequest request) {
        return reduce(checkConflictingDays(groupByDay(request.placeHolders())));
    }

    private final Collector<Map.Entry<Integer, DayDescription>, ?, Map<Integer, Set<DayDescription>>> toDaySet = Collectors
            .groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toUnmodifiableSet()));

    private Map<Integer, Set<DayDescription>> groupByDay(
            Map<DayDescription, ? extends Collection<Integer>> modifiersDays) {
        return modifiersDays.entrySet().stream().flatMap(e -> e.getValue().stream().map(v -> Map.entry(v, e.getKey())))
                .collect(toDaySet);
    }

    private Map<Integer, Set<DayDescription>> checkConflictingDays(Map<Integer, Set<DayDescription>> map) {
        Map<Integer, Set<DayDescription>> errors = map.entrySet().stream().filter(e -> e.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Conflicting Days " + errors);
        }
        return map;
    }

    private Map<Integer, DayDescription> reduce(Map<Integer, Set<DayDescription>> map) {
        return map.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> e.getValue().iterator().next()));
    }
}
