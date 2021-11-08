package com.cubicfox.attendance;

import com.cubicfox.attendance.imagemaker.AttendanceImageMaker;
import com.cubicfox.attendance.imagemaker.AttendanceProfile;
import com.cubicfox.attendance.imagemaker.AttendanceProfile.Placement;
import com.cubicfox.attendance.imagemaker.AttendanceRequestDTO;
import com.cubicfox.attendance.imagemaker.PlaceHolder;
import java.io.IOException;
import java.net.URI;
import java.time.YearMonth;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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

    @GetMapping
    ModelAndView form(@Valid FormRequest formRequest, BindingResult bindingResult, Model model) throws IOException {
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(e -> log.warn("{}", e));
        }
        return new ModelAndView("form", model.asMap());
    }

    @GetMapping(value = "/image", produces = MediaType.IMAGE_JPEG_VALUE)
    ResponseEntity<StreamingResponseBody> image(@Valid FormRequest formRequest, BindingResult bindingResult,
            Model model) throws IOException {
        if (bindingResult.hasErrors()) {
            String uri = uri(formRequest.params());
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(uri));
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).headers(headers).build();
        }
        List<Placement<?>> placements = attendanceProfile.createPlacements(AttendanceRequestDTO.from(formRequest.name,
                formRequest.yearMonth, formRequest.include, formRequest.placeHolders()));
        StreamingResponseBody rb = os -> imageMaker.write(placements, MediaType.IMAGE_JPEG_VALUE, os);
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

    @Value
    public static class FormRequest {
        @NotBlank
        String name;
        @NotNull
        YearMonth yearMonth;

        List<@Valid @Min(1) @Max(31) Integer> include;
        List<@Valid @Min(1) @Max(31) Integer> fs;
        List<@Valid @Min(1) @Max(31) Integer> bs;
        List<@Valid @Min(1) @Max(31) Integer> lo;

        Map<PlaceHolder, ? extends Collection<Integer>> placeHolders() {
            return Map.of(PlaceHolder.FS, fs, PlaceHolder.BS, bs, PlaceHolder.LO, lo);
        }

        Map<String, String> params() {
            var map = new HashMap<String, String>();
            addIf(map, "name", name);
            addIf(map, "yearMonth", yearMonth);
            addIf(map, "include", include);
            addIf(map, "fs", fs);
            addIf(map, "bs", bs);
            addIf(map, "lo", lo);
            return map;
        }

        private void addIf(Map<String, String> map, String key, Object object) {
            if (object == null) {
                return;
            }
            if (object instanceof Collection<?>) {
                Collection<?> c = (Collection<?>) object;
                if (!c.isEmpty()) {
                    map.put(key, convert(c));
                }
            } else
                map.put(key, String.valueOf(object));
        }

        private String convert(Collection<?> c) {
            return c.stream().filter(Objects::nonNull).map(String::valueOf).collect(Collectors.joining(","));
        }
    }
}
