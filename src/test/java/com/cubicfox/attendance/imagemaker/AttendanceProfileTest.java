package com.cubicfox.attendance.imagemaker;

import static org.assertj.core.api.Assertions.assertThat;

import com.cubicfox.attendance.imagemaker.AttendanceProfile.Placement;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

@Slf4j
@SpringBootTest(classes = AttendanceProfile.class)
class AttendanceProfileTest {

    @Autowired
    AttendanceProfile attendanceProfile;

    @Test
    void test() {
        AttendanceRequestDTO dto = AttendanceRequestDTO.from("name", YearMonth.of(2021, 1), List.of(1),
                Map.of(PlaceHolder.FS, List.of(4)));
        List<Placement<?>> placements = attendanceProfile.createPlacements(dto);
        Set<String> texts = placements.stream().map(Placement::getObject).map(String::valueOf)
                .collect(Collectors.toUnmodifiableSet());
        assertThat(texts).contains("name", "2021", "január", "FS", "8");
        this.write(placements);
    }

    private AttendanceImageMaker attendanceImageMaker = new AttendanceImageMaker();

    private void write(List<Placement<?>> placements) {
        Instant start = Instant.now();
        try (OutputStream os = new FileOutputStream(Files.createTempFile("test-attendance", ".jpeg").toFile())) {
            attendanceImageMaker.write(placements, MediaType.IMAGE_JPEG_VALUE, os);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        Instant end = Instant.now();
        log.info("format: {}; Duration: {}", MediaType.IMAGE_JPEG_VALUE, Duration.between(start, end));

    }

}
