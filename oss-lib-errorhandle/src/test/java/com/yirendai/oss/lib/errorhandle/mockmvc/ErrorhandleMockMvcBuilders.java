package com.yirendai.oss.lib.errorhandle.mockmvc;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;

/**
 * Created by haolun on 16/12/23.
 */
@NoArgsConstructor(access = PRIVATE)
public abstract class ErrorhandleMockMvcBuilders {

  public static ResultActions performRequestToErrorController(
    final MockMvc mockMvc, //
    final MvcResult originalMvcResult //
  ) throws Exception {
    return mockMvc.perform(requestToErrorController(originalMvcResult));
  }

  public static RequestBuilder requestToErrorController(final MvcResult originalMvcResult) {
    return new RequestBuilder() {

      @Override
      public MockHttpServletRequest buildRequest(final ServletContext servletContext) {
        return requestToErrorController("/error", originalMvcResult.getRequest());
      }
    };
  }

  static MockHttpServletRequest requestToErrorController(
    final String path,
    final MockHttpServletRequest originalRequest
  ) {
    originalRequest.setDispatcherType(DispatcherType.ERROR);
    originalRequest.setRequestURI(path);
    return originalRequest;
  }
}
