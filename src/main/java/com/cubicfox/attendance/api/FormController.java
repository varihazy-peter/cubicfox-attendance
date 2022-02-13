package com.cubicfox.attendance.api;

import com.cubicfox.attendance.imagemaker.AttendanceImageMaker;
import com.cubicfox.attendance.imagemaker.AttendanceProfile;
import com.cubicfox.attendance.imagemaker.AttendanceProfile.Placement;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.Channels;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Controller
public class FormController {

    AttendanceProfile attendanceProfile;
    AttendanceImageMaker imageMaker;
    FormRequestAdapter formRequestAdapter;

    @GetMapping
    ModelAndView form(@Valid FormRequest formRequest, BindingResult bindingResult, Model model) throws IOException {
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(e -> log.warn("{}", e));
            model.addAttribute("errors", bindingResult.getAllErrors().stream().map(ObjectError::toString)
                    .collect(Collectors.toUnmodifiableList()));
        }
        return new ModelAndView("form", model.asMap());
    }

    @GetMapping(value = "/image", produces = MediaType.IMAGE_JPEG_VALUE)
    ResponseEntity<StreamingResponseBody> image(@Valid FormRequest request, BindingResult bindingResult, Model model)
            throws IOException {
        if (bindingResult.hasErrors()) {
            String uri = uri(request.params());
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(uri));
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).headers(headers).build();
        }
        List<Placement<?>> placements = attendanceProfile.createPlacements(formRequestAdapter.map(request));
        StreamingResponseBody rb = os -> imageMaker.write(placements, MediaType.IMAGE_JPEG_VALUE,
                Channels.newChannel(os));
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(rb);
    }

    String uri(Map<String, String> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            builder = builder.queryParam(key, val);
        }
        return builder.toUriString();
    }
}
