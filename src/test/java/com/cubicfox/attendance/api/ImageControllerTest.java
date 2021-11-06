package com.cubicfox.attendance.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.cubicfox.attendance.api.ImageController.ImageRequest;
import com.cubicfox.attendance.imagemaker.AttendanceProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.YearMonth;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(controllers = ImageController.class)
@ComponentScan(basePackageClasses = AttendanceProfile.class)
class ImageControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void test_empty() throws Exception {
        mockMvc.perform( //
                request(HttpMethod.GET, "/rest/image").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.IMAGE_JPEG).content("{}")) //
                .andDo(MockMvcResultHandlers.print()) //
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    ImageRequest imageRequest = new ImageRequest("name", YearMonth.of(2020, 1), Map.of());
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void test_ok() throws Exception {
        String requestBody = objectMapper.writeValueAsString(imageRequest);
        MvcResult mvcResult = mockMvc.perform( //
                request(HttpMethod.GET, "/rest/image").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.IMAGE_JPEG).content(requestBody)) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.IMAGE_JPEG)) //
                .andReturn();
        byte[] content = mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.IMAGE_JPEG)).andReturn()
                .getResponse().getContentAsByteArray();
        assertThat(content).isNotNull().isNotEmpty().hasSizeGreaterThan(16 * 1024);
    }
}
