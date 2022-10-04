package com.cubicfox.attendance.api;

import com.cubicfox.attendance.WorkCalendar;
import com.cubicfox.attendance.imagemaker.AttendanceImageMaker;
import com.cubicfox.attendance.imagemaker.AttendanceProfile;
import com.cubicfox.attendance.imagemaker.AttendanceProfile.Placement;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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
    ModelAndView form(@Valid FormRequest formRequest, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(e -> log.warn("{}", e));
            model.addAttribute("errors", bindingResult.getAllErrors().stream().map(ObjectError::toString)
                    .collect(Collectors.toUnmodifiableList()));
        }
        Map<String, Object> modelMap = model.asMap();
        modelMap.put("workCalendar", Stream.concat( //
                Stream.of(new SelectOption("NONE", "", formRequest.getWorkCalendar() == null)),
                Arrays.stream(WorkCalendar.values()) //
                        .map(wc -> new SelectOption(wc.name(), wc.name(), formRequest.getWorkCalendar() == wc)))
                .toList());
        return new ModelAndView("form", modelMap);
    }

    @Value
    private static class SelectOption {
        String name;
        String value;
        boolean isSelected;

        @SuppressWarnings("unused")
        public String selected() {
            return isSelected ? "selected" : "";
        }
    }

    @GetMapping(value = "/image", produces = MediaType.IMAGE_JPEG_VALUE)
    ResponseEntity<StreamingResponseBody> image(@Valid FormRequest request, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return redirect();
        }
        List<Placement> placements = attendanceProfile.createPlacements(formRequestAdapter.map(request));
        StreamingResponseBody rb = os -> imageMaker.write(placements, MediaType.IMAGE_JPEG_VALUE, os);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(rb);
    }

    private ResponseEntity<StreamingResponseBody> redirect() {
        String queryString = (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest())
                .getQueryString();
        URI uri = UriComponentsBuilder.fromPath("/").query(queryString).build().toUri();
        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).header(HttpHeaders.LOCATION, uri.toString())
                .build();
    }
}
