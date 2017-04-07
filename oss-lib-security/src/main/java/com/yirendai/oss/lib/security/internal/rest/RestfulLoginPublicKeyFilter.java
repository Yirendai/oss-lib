package com.yirendai.oss.lib.security.internal.rest;

import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpMethod.GET;

import com.yirendai.oss.lib.common.crypto.KeyExpression;
import com.yirendai.oss.lib.errorhandle.internal.RestfulExceptionHandler;
import com.yirendai.oss.lib.webmvc.internal.DefaultHttpEntityMethodProcessor;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpEntity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Note: Not a bean, avoid auto pick-up.
 * Created by zhanghaolun on 16/7/11.
 */
@Slf4j
public class RestfulLoginPublicKeyFilter extends GenericFilterBean {

  private final KeyExpression key;

  private RequestMatcher requestMatcher;

  @Autowired
  @Setter
  private RestfulExceptionHandler exceptionHandler;
  @Autowired
  @Setter
  private DefaultHttpEntityMethodProcessor httpEntityMethodProcessor;

  public RestfulLoginPublicKeyFilter(final KeyExpression publicKey) {
    super();
    this.key = publicKey;
  }

  public KeyExpression getKey() {
    return this.key;
  }

  @Override
  public void doFilter( //
    final ServletRequest req, //
    final ServletResponse res, //
    final FilterChain chain //
  ) throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;

    if (!this.isKeyRequest(request, response)) {
      chain.doFilter(request, response);
    } else {
      final HttpEntity<KeyExpression> returnValue = new HttpEntity<>(this.getKey());
      final Method method = ClassUtils.getMethod(RestfulLoginPublicKeyFilter.class, "getKey");
      final MethodParameter returnTypeMethodParam = new MethodParameter(method, -1);
      final MethodParameter returnType = new MethodParameter(returnTypeMethodParam);
      final ModelAndViewContainer mavContainer = new ModelAndViewContainer();
      final NativeWebRequest webRequest = new ServletWebRequest(request, response);
      try {
        checkState(isNotBlank(this.key.getValue()), "The key you request is not present.");
        this.httpEntityMethodProcessor.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
      } catch (final Exception ex) {
        if (log.isWarnEnabled()) {
          log.warn("Failed to process. Output a error response: {}", returnValue, ex);
        }
        this.exceptionHandler.resolveAndHandle(request, response, ex);
      }
    }
  }

  @SuppressWarnings({"squid:S1172"})
  protected boolean isKeyRequest( //
    final HttpServletRequest request, //
    final HttpServletResponse response //
  ) {
    return this.requestMatcher.matches(request);
  }

  public void setRequestMatcher(final RequestMatcher requestMatcher) {
    Assert.notNull(requestMatcher, "requestMatcher cannot be null");
    this.requestMatcher = requestMatcher;
  }

  public void setFilterProcessesUrl(final String filterProcessesUrl) {
    this.requestMatcher = new AntPathRequestMatcher(filterProcessesUrl, GET.name());
  }
}
