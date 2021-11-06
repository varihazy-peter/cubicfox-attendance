package com.cubicfox.attendance.imagemaker;

import java.awt.Font;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Component
public class AttendanceProfile {

    String st = "09:00";
    String et = "17:30";
    String h = "8";
    Font fontT = new Font("Monospaced", Font.PLAIN, 60);
    Font fontH = new Font("Monospaced", Font.PLAIN, 140);

    @Value
    public static class Placement<T> {
        T object;
        int x, y;
        java.awt.Font font;
    }

    public List<Placement<?>> createPlacements(AttendanceRequestDTO requestDTO) {
        Predicate<LocalDate> leaveOutFilter = d -> requestDTO.getPlaceHolders().containsKey(d.getDayOfMonth())
                || (d.getDayOfWeek().getValue() < 6);
        Stream<Placement<?>> days = requestDTO.getYearMonth().atDay(1)
                .datesUntil(requestDTO.getYearMonth().plusMonths(1).atDay(1)).filter(leaveOutFilter)
                .map(LocalDate::getDayOfMonth)
                .flatMap(day -> placeDay(day, requestDTO.getPlaceHolders().get(day)).stream());
        Stream<Placement<?>> head = placeHead(requestDTO.getName(), requestDTO.getYearMonth()).stream();
        return Stream.concat(head, days).collect(Collectors.toUnmodifiableList());
    }

    private List<Placement<?>> placeHead(String name, YearMonth ym) {
        String month = ym.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, new Locale("hu", "HU"));
        return List.of(placeText(name, 480, 680, fontT), placeText(ym.getYear(), 1250, 680, fontT),
                placeText(month, 1510, 680, fontT));
    }

    private List<Placement<String>> placeDay(int day, PlaceHolder ph) {
        if (ph == null) {
            return List.of(placeText(st, calculateCord(day, Offset.Time1), fontT),
                    placeText(et, calculateCord(day, Offset.Time2), fontT),
                    placeText(h, calculateCord(day, Offset.TimeH), fontH));
        } else {
            return List.of( //
                    placeText(ph.getText(), calculateCord(day, Offset.FS), fontH));
        }
    }

    private <T> Placement<T> placeText(T object, Point point, Font font) {
        return placeText(object, point.x, point.y, font);
    }

    private <T> Placement<T> placeText(T object, int x, int y, Font font) {
        return new Placement<T>(object, x, y, font);
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
        Time1(0, 0), Time2(0, 67), TimeH(440, 67), FS(390, 67);

        int x, y;
    }

    @Value
    private static class Point {
        int x, y;
    }

}
