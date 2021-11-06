package com.cubicfox.attendance.api;

import com.cubicfox.attendance.imagemaker.AttendanceImageMaker;
import com.cubicfox.attendance.imagemaker.AttendanceProfile;
import com.cubicfox.attendance.imagemaker.AttendanceProfile.Placement;
import com.cubicfox.attendance.imagemaker.AttendanceRequestDTO;
import com.cubicfox.attendance.imagemaker.PlaceHolder;
import java.io.IOException;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RestController
public class ImageController {

    AttendanceProfile attendanceProfile;
    AttendanceImageMaker imageMaker;

    @GetMapping(value = "/rest/image", produces = MediaType.IMAGE_JPEG_VALUE)
    ResponseEntity<StreamingResponseBody> image(@Valid ImageRequest imageRequest) throws IOException {
        List<Placement<?>> placements = attendanceProfile.createPlacements(imageRequest.attendanceRequest());
        StreamingResponseBody rb = os -> imageMaker.write(placements, MediaType.IMAGE_JPEG_VALUE, os);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(rb);
    }

    @Value
    static class ImageRequest {
        @NotBlank
        String name;
        @NotNull
        YearMonth yearMonth;
        Map<@Valid @Min(1) @Max(31) Integer, PlaceHolder> placeHolders;

        AttendanceRequestDTO attendanceRequest() {
            return new AttendanceRequestDTO(getName(), getYearMonth(), Set.of(), getPlaceHolders());
        }
    }
}
