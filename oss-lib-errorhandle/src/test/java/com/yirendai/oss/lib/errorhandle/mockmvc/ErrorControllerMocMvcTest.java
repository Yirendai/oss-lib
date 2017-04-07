package com.yirendai.oss.lib.errorhandle.mockmvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Created by Meifans on 16/12/21.
 */
@ContextConfiguration(classes = {ErrorhandleMockMvcConfiguration.class})
@DirtiesContext
//@EnableSpringDataWebSupport
@RunWith(SpringRunner.class)
//@SpringBootTest
@WebMvcTest(MakeErrorsRestController.class)
@Slf4j
public class ErrorControllerMocMvcTest {

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext wac;

  @Before
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  public void testStatusCodeAndMessage() throws Exception {
    // In a real container the response is carried over into the error dispatcher, but
    // in the mock a new one is created so we have to assert the status at this
    // intermediate point
    final MvcResult result = this.mockMvc.perform(get("/nest"))
      .andExpect(status().isNotFound()).andReturn();

    final MvcResult response = this.mockMvc.perform(ErrorhandleMockMvcBuilders.requestToErrorController(result))
      .andExpect(jsonPath("@.localizedMessage").value("Exception"))
      .andExpect(jsonPath("message").value("go")).andReturn();

    // And the rendered status code is always wrong (but would be 400 in a real
    // system)
    String content = response.getResponse().getContentAsString();
    log.info("content:" + content);
  }
}
