package com.cubicfox.attendance.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.cubicfox.attendance.AttendanceCalendar;
import com.cubicfox.attendance.imagemaker.AttendanceProfile;
import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

@WebMvcTest(controllers = FormController.class)
@ComponentScan(basePackageClasses = { AttendanceProfile.class, FormController.class, AttendanceCalendar.class })
class FormControllerTest {

    @Autowired
    MockMvc mockMvc;
    FormRequest.FormRequestBuilder base = FormRequest.builder().name("name").yearMonth(YearMonth.of(2020, 10));

    @Test
    void test_nameAndYM() throws Exception {
        assertFormRequest("/", FormRequest.builder().build());
        assertFormRequest("/?name=name&yearMonth=2020-10", base.build());
        assertFormRequest("/?name=name&yearMonth=2020-10&hours=1&fs=2&bs=3&lo=4",
                base.hours(List.of(1)).fs(List.of(2)).bs(List.of(3)).lo(List.of(4)).build());
    }

    void assertFormRequest(String uri, FormRequest expected) throws Exception {
        ModelAndView modelAndView = mockMvc.perform(get(URI.create(uri))).andExpect(status().isOk())
                .andExpect(view().name("form")).andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andReturn().getModelAndView();
        assertThat(modelAndView).isNotNull();
        FormRequest formRequest = (FormRequest) modelAndView.getModel().get("formRequest");
        assertThat(formRequest).isNotNull().isEqualTo(expected);
    }

    @Test
    void test_image_redirect() throws Exception {
        Map<String, String> queryParam = Map.of("name", "name", "lo", "4");
        URI uri = UriComponentsBuilder.fromPath("/image")
                .queryParams(new MultiValueMapAdapter<>(queryParam.entrySet().stream()
                        .reduce(ImmutableMap.<String, List<String>> builder(),
                                (b, e) -> b.put(e.getKey(), List.of(e.getValue())), (b, c) -> b.putAll(c.build()))
                        .build()))
                .build().toUri();

        String location = mockMvc.perform(get(uri)).andExpect(status().is3xxRedirection()).andReturn().getResponse()
                .getHeader("Location");
        assertThat(UriComponentsBuilder.fromUriString(location).build().getQueryParams().toSingleValueMap())
                .isEqualTo(queryParam);
    }

    @Test
    void test_image_ok() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(URI.create("/image?name=name&yearMonth=2020-11&hours=1&fs=2&bs=3&lo=4")))
                .andExpect(status().isOk()).andReturn();
        byte[] content = mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.IMAGE_JPEG)).andReturn()
                .getResponse().getContentAsByteArray();
        assertThat(content).isNotNull().isNotEmpty().hasSizeGreaterThan(169928);
    }
}
