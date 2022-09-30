package com.cubicfox.attendance;

import com.cubicfox.attendance.domain.DayDescription;
import com.cubicfox.attendance.domain.DayModifier;
import java.time.YearMonth;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendanceCalendar {
    public Map<Integer, DayDescription> calculateDaysModifiers(final YearMonth yearMonth,
            final Map<Integer, DayDescription> base) {
        return IntStream.rangeClosed(1, yearMonth.atEndOfMonth().getDayOfMonth())
                .mapToObj(d -> calculateDayIfneeded(yearMonth, d, base.get(d)))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    }

    private Map.Entry<Integer, DayDescription> calculateDayIfneeded(final YearMonth yearMonth, int dayOfMonth,
            final DayDescription modifier) {
        return Map.entry(dayOfMonth, modifier != null ? modifier : this.calculateDayModifier(yearMonth, dayOfMonth));
    }

    /**
     * The logic to calculate the a day modifier is to look up if it is a weekend or not. This works most of cases. In
     * case of more complex logic needed (like calling an holiday API the calculation require an additional component).
     */
    private DayDescription calculateDayModifier(final YearMonth yearMonth, int dayOfMonth) {
        return yearMonth.atDay(dayOfMonth).getDayOfWeek().getValue() < 6 ? DayDescription.H8 : DayModifier.LO;
    }
}
