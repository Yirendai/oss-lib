package com.yirendai.oss.lib.security.internal.feign;

import static com.google.common.base.Preconditions.checkState;
import static com.yirendai.oss.lib.security.api.Security.HEADER_AUTH_TOKEN;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.yirendai.oss.lib.security.api.GenericUser;
import com.yirendai.oss.lib.security.internal.AuthenticationTokens;
import com.yirendai.oss.lib.webmvc.api.TypeSafeToken;

import feign.RequestInterceptor;
import feign.RequestTemplate;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

/**
 * see: https://github.com/spring-cloud/spring-cloud-netflix/pull/1093
 * see: https://github.com/spring-cloud/spring-cloud-security/issues/87
 * see: https://github.com/spring-cloud/spring-cloud-netflix/issues/1336
 */
@Slf4j
public class ContextAuthTokenFeignRequestInterceptor implements RequestInterceptor, InitializingBean {

  @Autowired
  @Setter
  private Environment environment;

  @Setter
  private TypeSafeToken<GenericUser> token;

  @Override
  public void apply(final RequestTemplate template) {
    final String token = AuthenticationTokens.tokenFromSecurityContext(this.token);

    if (token != null) {
      template.header(HEADER_AUTH_TOKEN, token);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    checkState(this.environment != null, "environment not set.");
    // see: HystrixSecurityAutoConfiguration
    final String hystrixShareSecurityContext = this.environment.getProperty("hystrix.shareSecurityContext");
    if (isBlank(hystrixShareSecurityContext)) {
      log.warn("hystrix.shareSecurityContext not set, RPC can not carry user token.");
    }
  }
}
