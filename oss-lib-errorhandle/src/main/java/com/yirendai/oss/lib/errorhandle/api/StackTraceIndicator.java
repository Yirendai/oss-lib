package com.yirendai.oss.lib.errorhandle.api;

import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by zhanghaolun on 16/8/15.
 */
public interface StackTraceIndicator {

  /**
   * is trace enabled.
   *
   * @param request  request nullable
   * @param produces produces nullable
   * @return stackTrace
   */
  Boolean stackTrace(HttpServletRequest request, MediaType produces);
}
