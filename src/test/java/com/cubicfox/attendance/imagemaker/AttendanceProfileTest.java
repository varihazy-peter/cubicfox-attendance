package com.cubicfox.attendance.imagemaker;

import static org.assertj.core.api.Assertions.assertThat;

import com.cubicfox.attendance.domain.DayDescription;
import com.cubicfox.attendance.domain.DayModifier;
import com.cubicfox.attendance.domain.MonthlyAttendance;
import com.cubicfox.attendance.imagemaker.AttendanceProfile.Placement;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;

@Slf4j
@SpringBootTest(classes = { AttendanceProfile.class, FontStoregeFactory.class }, webEnvironment = WebEnvironment.NONE)
class AttendanceProfileTest {

    @Autowired
    AttendanceProfile attendanceProfile;
    Map<Integer, DayDescription> days = Map.of(1, DayModifier.LO, 3, DayModifier.H8, 4, DayModifier.FS, 5, DayModifier.H9, 6, DayModifier.H9_5);

    @Test
    void test() {
        MonthlyAttendance dto = new MonthlyAttendance("name", YearMonth.of(2021, 1), days);
        List<Placement> placements = attendanceProfile.createPlacements(dto);
        Map<String, Long> texts = placements.stream().map(Placement::getText)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        log.info("{}", texts);
        assertThat(texts).containsAllEntriesOf(
                Map.of("január", 1l, "8", 1l, "name", 1l, "2021", 1l, "17:30", 1l, "FS", 1l, "09:00", 3l));

        this.write(placements);
    }

    private AttendanceImageMaker attendanceImageMaker = new AttendanceImageMaker();

    private void write(List<Placement> placements) {
        Instant start = Instant.now();
        try (OutputStream os = new FileOutputStream(Files.createTempFile("test-attendance", ".jpeg").toFile())) {
            attendanceImageMaker.write(placements, MediaType.IMAGE_JPEG_VALUE, Channels.newChannel(os));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        Instant end = Instant.now();
        log.info("format: {}; Duration: {}", MediaType.IMAGE_JPEG_VALUE, Duration.between(start, end));

    }

}
