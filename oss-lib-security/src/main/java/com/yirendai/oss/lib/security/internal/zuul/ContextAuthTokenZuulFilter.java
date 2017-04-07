package com.yirendai.oss.lib.security.internal.zuul;

import static com.yirendai.oss.lib.security.api.Security.HEADER_AUTH_TOKEN;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.yirendai.oss.lib.security.api.GenericUser;
import com.yirendai.oss.lib.security.internal.AuthenticationTokens;
import com.yirendai.oss.lib.webmvc.api.TypeSafeToken;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


/**
 * 决定请求是否被zuul代理的关键是{@link org.springframework.cloud.netflix.zuul.filters.RouteLocator}
 * 和 {@link org.springframework.cloud.netflix.zuul.web.ZuulHandlerMapping}
 * <p>
 * to disable in yml: zuul.SimpleClassName.filterType.disable=true
 * </p>
 * <p>
 * example: {@link org.springframework.cloud.netflix.zuul.filters.post.SendResponseFilter}
 * </p>
 */
@Slf4j
public class ContextAuthTokenZuulFilter extends ZuulFilter {

  @Setter
  private TypeSafeToken<GenericUser> token;

  @Override
  public String filterType() {
    return "pre";
  }

  @Override
  public int filterOrder() {
    return 10;
  }

  @Override
  public boolean shouldFilter() {
    final Boolean shouldFilter = AuthenticationTokens.tokenInSecurityContext();

    if (log.isTraceEnabled()) {
      log.trace("shouldFilter: {}", shouldFilter);
    }

    return shouldFilter;
  }

  @Override
  public Object run() {
    final String token = AuthenticationTokens.tokenFromSecurityContext(this.token);

    if (log.isTraceEnabled()) {
      log.trace("addZuulRequestHeader: {}: {}", HEADER_AUTH_TOKEN, token);
    }

    final RequestContext ctx = RequestContext.getCurrentContext();
    ctx.addZuulRequestHeader(HEADER_AUTH_TOKEN, token);
    return null;
  }
}
