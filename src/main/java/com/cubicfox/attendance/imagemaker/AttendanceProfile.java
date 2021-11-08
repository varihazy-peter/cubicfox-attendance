package com.cubicfox.attendance.imagemaker;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public class AttendanceProfile {

    String st = "09:00";
    String et = "17:30";
    String h = "8";
    Font defaultFont;
    {
        try {
            java.io.InputStream inputStream = AttendanceProfile.class.getResourceAsStream("/Inconsolata-Regular.ttf");
            defaultFont = Font.createFont(Font.PLAIN, inputStream);
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    Font fontN = defaultFont.deriveFont(82f);
    Font fontT = defaultFont.deriveFont(72f);
    Font fontH = defaultFont.deriveFont(180f);
    Font fontDOW = defaultFont.deriveFont(40f);

    @Value
    public static class Placement<T> {
        @NonNull
        T object;
        int x, y;
        @NonNull
        java.awt.Font font;

        public String text() {
            return String.valueOf(this.object);
        }
    }

    public List<Placement<?>> createPlacements(AttendanceRequestDTO requestDTO) {
        Stream<Placement<?>> days = requestDTO.getYearMonth().atDay(1)
                .datesUntil(requestDTO.getYearMonth().plusMonths(1).atDay(1)) //
                .flatMap(d -> placeDate(d, placeHolder(d, requestDTO.getPlaceHolders())).stream());
        Stream<Placement<?>> head = placeHead(requestDTO.getName(), requestDTO.getYearMonth()).stream();
        return Stream.concat(head, days).collect(Collectors.toUnmodifiableList());
    }

    private final Point pointName = new Point(480, 680);
    private final Point pointYear = new Point(1250, 680);
    private final Point pointMonth = new Point(1510, 680);

    private List<Placement<?>> placeHead(String name, YearMonth ym) {
        String month = ym.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, new Locale("hu", "HU"));
        return List.of(placeText(name, pointName, fontN), placeText(ym.getYear(), pointYear, fontN),
                placeText(month, pointMonth, fontN));
    }

    private PlaceHolder placeHolder(LocalDate date, final Map<Integer, PlaceHolder> placeHolders) {
        PlaceHolder placeHolder = placeHolders == null ? null : placeHolders.get(date.getDayOfMonth());
        if (placeHolder != null) {
            return placeHolder;
        }
        return date.getDayOfWeek().getValue() < 6 ? PlaceHolder.H8 : PlaceHolder.LO;
    }

    private List<Placement<String>> placeDate(LocalDate date, PlaceHolder placeHolder) {
        int day = date.getDayOfMonth();
        switch (placeHolder) {
        case LO:
            return List.of(placeText(date.getDayOfWeek().name(), calculateCord(day, Offset.DOW), fontDOW));
        case FS:
        case BS:
            return List.of( //
                    placeText(placeHolder.getText(), calculateCord(day, Offset.FS), fontH),
                    placeText(date.getDayOfWeek().name(), calculateCord(day, Offset.DOW), fontDOW));
        case H8:
            return List.of( //
                    placeText(st, calculateCord(day, Offset.Time1), fontT),
                    placeText(et, calculateCord(day, Offset.Time2), fontT),
                    placeText(h, calculateCord(day, Offset.TimeH), fontH),
                    placeText(date.getDayOfWeek().name(), calculateCord(day, Offset.DOW), fontDOW));
        default:
            throw new IllegalStateException("not handled placeHolder " + placeHolder);
        }
    }

    private <T> Placement<T> placeText(T object, Point point, Font font) {
        return placeText(object, point.x, point.y, font);
    }

    private <T> Placement<T> placeText(T object, int x, int y, Font font) {
        return new Placement<>(object, x, y, font);
    }

    private Point calculateCord(int day, Offset offset) {
        if (day < 0 || day > 31) {
            throw new IllegalArgumentException("day must be between 1 and 31");
        }
        int dayYPos = (day < 16) ? day - 1 : day - 16;
        int dayXCord = (day < 16) ? 610 : 1632;
        return new Point(dayXCord + offset.x, dayYPos * 135 + 890 + offset.y);
    }

    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static enum Offset {
        Time1(0, 0), Time2(0, 67), TimeH(440, 67), FS(390, 67), DOW(-386, 67);

        int x, y;
    }

    @Value
    private static class Point {
        int x, y;
    }

}
