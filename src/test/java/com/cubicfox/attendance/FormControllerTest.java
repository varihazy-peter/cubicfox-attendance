package com.cubicfox.attendance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.cubicfox.attendance.imagemaker.AttendanceImageMaker;
import com.cubicfox.attendance.imagemaker.AttendanceProfile;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ModelAndView;

@AutoConfigureMockMvc
@SpringBootTest(classes = { FormController.class, AttendanceProfile.class, AttendanceImageMaker.class })
class FormControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void test() throws Exception {
        ModelAndView modelAndView = mockMvc.perform(get(URI.create("/"))).andExpect(status().isOk())
                .andExpect(view().name("form")).andDo(print()).andReturn().getModelAndView();
        assertThat(modelAndView).isNotNull();
    }

}
