package com.yirendai.oss.lib.errorhandle.internal;

import static com.yirendai.oss.lib.errorhandle.api.ExceptionResolver.COMPOSITE_EXCEPTION_RESOLVER;

import com.yirendai.oss.lib.errorhandle.api.ExceptionResolver;
import com.yirendai.oss.lib.errorhandle.api.ResolvedError;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by zhanghaolun on 16/8/22.
 */
public class TemplateExceptionHandler {

  private String errorPage;

  @Qualifier(COMPOSITE_EXCEPTION_RESOLVER)
  @Autowired
  @Setter
  private ExceptionResolver<Throwable> exceptionResolver;

  public ModelAndView resolveAndHandle( //
    final HttpServletRequest request, //
    final HttpServletResponse response, //
    final Throwable throwable //
  ) {
    final ResolvedError resolvedError = this.exceptionResolver.resolve(request, throwable);
    return this.handleResolved(request, response, resolvedError);
  }

  public ModelAndView handleResolved( //
    final HttpServletRequest request, //
    final HttpServletResponse response, //
    final ResolvedError resolvedError //
  ) {
    final ModelAndView mav = new ModelAndView(this.errorPage);
    mav.addObject(ResolvedError.RESOLVED_ERROR_OBJECT, resolvedError);
    return mav;
  }

  public void setErrorPage(final String errorPage) {
    this.errorPage = errorPage;
  }
}
