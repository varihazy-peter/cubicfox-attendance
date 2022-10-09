package com.cubicfox.attendance.imagemaker;

import com.cubicfox.attendance.domain.DayDescription;
import com.cubicfox.attendance.domain.MonthlyAttendance;
import java.awt.Font;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Component
public class AttendanceProfile {

    FontStorege fontStorege;

    @Value
    public static class Placement {
        @NonNull
        String text;
        int x;
        int y;
        @NonNull
        java.awt.Font font;

        public String text() {
            return text;
        }
    }

    public List<Placement> createPlacements(MonthlyAttendance requestDTO) {
        Stream<Placement> days = requestDTO.getDays().entrySet().stream()
                .flatMap(e -> this.placeDate(requestDTO.getYearMonth().atDay(e.getKey()), e.getValue()).stream());
        Stream<Placement> head = placeHead(requestDTO.getName(), requestDTO.getYearMonth()).stream();
        return Stream.concat(head, days).collect(Collectors.toUnmodifiableList());
    }

    private List<Placement> placeHead(String name, YearMonth ym) {
        String month = ym.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, new Locale("hu", "HU"));
        return List.of( //
                placeText(name, 480, 680, fontStorege.getFontN()), //
                placeText(Integer.toString(ym.getYear()), 1250, 680, fontStorege.getFontN()), //
                placeText(month, 1510, 680, fontStorege.getFontN()) //
        );
    }

    private List<Placement> placeDate(LocalDate date, DayDescription dayDescription) {
        int day = date.getDayOfMonth();
        return Stream.of( //
                placeText(date.getDayOfWeek().name(), day, Offset.DOW, fontStorege.getFontDOW()), //
                placeText(dayDescription.getText(), day, Offset.HOURS, fontStorege.getFontH()), //
                placeText(dayDescription.getStart(), day, Offset.START, fontStorege.getFontT()), //
                placeText(dayDescription.getEnd(), day, Offset.END, fontStorege.getFontT()) //
        ).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Placement placeText(String text, int day, Offset offset, Font font) {
        if (text == null || text.length() == 0) {
            return null;
        }
        if (day < 0 || day > 31) {
            throw new IllegalArgumentException("day must be between 1 and 31");
        }
        int baseY = ((day < 16) ? day - 1 : day - 16 ) * 135 + 890;
        int baseX = (day < 16) ? 610 : 1632;
        return placeText(text, baseX + offset.xFor(text.length()), baseY + offset.y, font);
    }

    private Placement placeText(String object, int x, int y, Font font) {
        return new Placement(object, x, y, font);
    }

    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private enum Offset {
        START(0, 0), END(0, 67), HOURS(440, 67, 50), DOW(-386, 67);

        int x;
        int y;
        int xOffset;

        private Offset(int x, int y) {
            this(x, y, 0);
        }

        int xFor(int n) {
            return n > 1 ? x - (n - 1) * xOffset : x;
        }
    }
}
