package com.yirendai.oss.lib.webmvc.internal;

import static lombok.AccessLevel.PACKAGE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.yirendai.oss.lib.webmvc.api.RequestResolver;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by zhanghaolun on 16/8/18.
 */
@Getter
@Setter(PACKAGE)
@Slf4j
public class DefaultRequestResolver implements RequestResolver {

  private String loginEndpoint;

  public DefaultRequestResolver(final String loginEndpoint) {
    this.loginEndpoint = loginEndpoint;
  }

  @Override
  public Boolean isLoginRequest(final HttpServletRequest request) {
    final String reqContextPath = request.getContextPath();
    final String contextPath = reqContextPath.endsWith("/") ? reqContextPath : reqContextPath + "/";
    final boolean isLoginRequest = request.getRequestURI().equals(contextPath + this.loginEndpoint);

    if (log.isTraceEnabled()) {
      log.trace("contextPath: {}, requestUri: {}, isLoginRequest: {}", //
          request.getContextPath(), request.getRequestURI(), isLoginRequest);
    }
    return isLoginRequest;
  }

  /**
   * TODO 进一步改进.
   */
  @Override
  public Boolean isAjaxRequest(final HttpServletRequest request) {
    final HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
    // final String contentTypeHeader = request.getHeader(CONTENT_TYPE);
    // final String content = isNotBlank(contentTypeHeader) ? contentTypeHeader : "";
    final String acceptHeader = request.getHeader(ACCEPT);
    final String accept = isNotBlank(acceptHeader) ? acceptHeader : "";
    final String underscore = request.getParameter("_");
    final boolean result = POST == httpMethod || PUT == httpMethod || DELETE == httpMethod //
        || accept.contains(APPLICATION_JSON_VALUE) //
        || isNotBlank(underscore);
    return result;
  }
}
