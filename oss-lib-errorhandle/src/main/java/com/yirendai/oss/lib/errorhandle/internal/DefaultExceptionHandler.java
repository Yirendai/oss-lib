package com.yirendai.oss.lib.errorhandle.internal;

import com.yirendai.oss.lib.webmvc.api.RequestResolver;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ControllerAdvice // for @ExceptionHandler methods
public class DefaultExceptionHandler {

  @Autowired
  @Setter
  private RestfulExceptionHandler restfulExceptionHandler;
  @Autowired
  @Setter
  private TemplateExceptionHandler templateExceptionHandler;
  @Autowired
  @Setter
  private RequestResolver requestResolver;

  @ExceptionHandler(Throwable.class)
  public ModelAndView resolveAndHandle( //
      final HttpServletRequest request, //
      final HttpServletResponse response, //
      final Throwable throwable) {
    final ModelAndView result;
    if (this.requestResolver.isAjaxRequest(request)) {
      result = this.restfulExceptionHandler.resolveAndHandle(request, response, throwable);
    } else {
      result = this.templateExceptionHandler.resolveAndHandle(request, response, throwable);
    }
    return result;
  }
}
