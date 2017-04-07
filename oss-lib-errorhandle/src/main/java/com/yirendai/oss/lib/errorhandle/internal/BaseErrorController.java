package com.yirendai.oss.lib.errorhandle.internal;

import com.yirendai.oss.lib.errorhandle.api.HttpHeader;
import com.yirendai.oss.lib.errorhandle.api.ResolvedError;
import com.yirendai.oss.lib.errorhandle.api.StackTraceIndicator;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * override default BasicErrorController.
 *
 * {@link org.springframework.boot.autoconfigure.web.BasicErrorController}
 *
 * <p>
 * see: ErrorMvcAutoConfiguration, BasicErrorController
 * </p>
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public abstract class BaseErrorController extends org.springframework.boot.autoconfigure.web.AbstractErrorController {

  private final ErrorProperties errorProperties;

  @Autowired
  @Setter
  private StackTraceIndicator traceIndicator;

  protected BaseErrorController( //
    final ExtendedErrorAttributes errorAttributes, //
    final ErrorProperties errorProperties //
  ) {
    super(errorAttributes);
    Assert.notNull(errorProperties, "AppErrorProperties must not be null");
    this.errorProperties = errorProperties;
  }

  protected ModelAndView errorHtml( //
    final HttpServletRequest request, //
    final HttpServletResponse response //
  ) {
    final Map<String, Object> errorAttributes = getErrorAttributes( //
      request, //
      isIncludeStackTrace(request, MediaType.TEXT_HTML) //
    );
    final ResolvedError resolvedError = ResolvedError.fromErrorAttributes(errorAttributes);
    final Integer status = resolvedError.getStatus();// this.getStatus(request)
    response.setStatus(status);
    // goto WhitelabelErrorViewConfiguration.defaultErrorView if not provided.
    final ModelAndView mav = new ModelAndView("error", resolvedError.toErrorAttributes());
    mav.addObject(ResolvedError.RESOLVED_ERROR_OBJECT, resolvedError);
    return mav;
  }

  protected ResponseEntity<ResolvedError> error(final HttpServletRequest request, final HttpServletResponse response) {
    final Map<String, Object> errorAttributes = getErrorAttributes( //
      request, //
      isIncludeStackTrace(request, MediaType.ALL) //
    );
    final ResolvedError body = ResolvedError.fromErrorAttributes(errorAttributes);
    final HttpStatus status = body.getHttpStatus();// this.getStatus(request);
    copyHeaders(body, response);
    return new ResponseEntity<>(body, status);
  }

  /**
   * copy ResolvedError的header到response.
   *
   * @param source   source
   * @param response response
   */
  private void copyHeaders(final ResolvedError source, final HttpServletResponse response) {
    if (source != null && source.getHeaders() != null) {
      for (final HttpHeader header : source.getHeaders()) {
        for (final String value : header.getValues()) {
          response.addHeader(header.getName(), value);
        }
      }
    }
  }

  protected boolean isIncludeStackTrace(final HttpServletRequest request, final MediaType produces) {
    return this.traceIndicator.stackTrace(request, null);
  }

  @Override
  public String getErrorPath() {
    return this.errorProperties.getPath();
  }

  public static class RestfulErrorController extends BaseErrorController {

    public RestfulErrorController( //
      final ExtendedErrorAttributes errorAttributes, //
      final ErrorProperties errorProperties //
    ) {
      super(errorAttributes, errorProperties);
    }

    @RequestMapping
    @ResponseBody
    public ResponseEntity<ResolvedError> error(final HttpServletRequest request, final HttpServletResponse response) {
      return super.error(request, response);
    }
  }

  public static class TemplateErrorController extends BaseErrorController {

    public TemplateErrorController( //
      final ExtendedErrorAttributes errorAttributes, //
      final ErrorProperties errorProperties //
    ) {
      super(errorAttributes, errorProperties);
    }

    @RequestMapping
    public ModelAndView errorHtml( //
      final HttpServletRequest request, //
      final HttpServletResponse response //
    ) {
      return super.errorHtml(request, response);
    }
  }

  public static class MixedErrorController extends BaseErrorController {

    public MixedErrorController( //
      final ExtendedErrorAttributes errorAttributes, //
      final ErrorProperties errorProperties //
    ) {
      super(errorAttributes, errorProperties);
    }

    @RequestMapping(produces = "text/html")
    public ModelAndView errorHtml( //
      final HttpServletRequest request, //
      final HttpServletResponse response //
    ) {
      return super.errorHtml(request, response);
    }

    @RequestMapping
    @ResponseBody
    public ResponseEntity<ResolvedError> error(final HttpServletRequest request, final HttpServletResponse response) {
      return super.error(request, response);
    }
  }
}
