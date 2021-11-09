package com.cubicfox.attendance.api;

import com.cubicfox.attendance.FormController;
import com.cubicfox.attendance.imagemaker.AttendanceImageMaker;
import com.cubicfox.attendance.imagemaker.AttendanceProfile;
import com.cubicfox.attendance.imagemaker.AttendanceProfile.Placement;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RestController
public class ImageController {

    AttendanceProfile attendanceProfile;
    AttendanceImageMaker imageMaker;

    @GetMapping(value = "/rest/image", produces = MediaType.IMAGE_JPEG_VALUE)
    ResponseEntity<StreamingResponseBody> image(@Valid FormController.FormRequest imageRequest) throws IOException {
        List<Placement<?>> placements = attendanceProfile.createPlacements(imageRequest.attendanceRequest());
        StreamingResponseBody rb = os -> imageMaker.write(placements, MediaType.IMAGE_JPEG_VALUE, os);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(rb);
    }

    @Value
    static class Valami {
        Map<String, String> errors;
    }
}
