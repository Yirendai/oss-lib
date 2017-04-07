package org.springframework.security.config.annotation.web.configurers;

import static com.yirendai.oss.lib.security.internal.template.TemplateAuthenticationEntryPoint.templateEntryPoint;
import static org.springframework.util.ClassUtils.isAssignableValue;
import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.getField;
import static org.springframework.util.ReflectionUtils.makeAccessible;

import com.yirendai.oss.lib.errorhandle.api.ExceptionResolver;
import com.yirendai.oss.lib.errorhandle.api.ResolvedError;
import com.yirendai.oss.lib.security.internal.template.EncryptDefaultLoginPageGeneratingFilter;
import com.yirendai.oss.lib.security.internal.template.TemplateAuthenticationEntryPoint;
import com.yirendai.oss.lib.webmvc.api.TypeSafeCookie;

import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by zhanghaolun on 16/11/9.
 */
public class EncryptDefaultLoginPageConfigurer<H extends HttpSecurityBuilder<H>> extends
  AbstractHttpConfigurer<DefaultLoginPageConfigurer<H>, H> {

  private String loginPage;
  private DefaultLoginPageGeneratingFilter loginPageGeneratingFilter;
  private Boolean useDefaultLoginPage;
  private ExceptionResolver<Throwable> exceptionResolver;
  private TypeSafeCookie<ResolvedError> resolvedErrorCookie;

  public EncryptDefaultLoginPageConfigurer( //
    final String loginPage, //
    final String publicKey, //
    final Boolean useDefaultLoginPage, //
    final ExceptionResolver<Throwable> exceptionResolver, //
    final TypeSafeCookie<ResolvedError> resolvedErrorCookie //
  ) {
    final EncryptDefaultLoginPageGeneratingFilter filter = new EncryptDefaultLoginPageGeneratingFilter();
    filter.setRsaPublicKey(publicKey);
    filter.setLoginPageUrl(loginPage);
    this.loginPage = loginPage;
    this.loginPageGeneratingFilter = filter;
    this.useDefaultLoginPage = useDefaultLoginPage;

    this.exceptionResolver = exceptionResolver;
    this.resolvedErrorCookie = resolvedErrorCookie;
  }

  @Override
  public void init(final H http) throws Exception {
    http.setSharedObject(DefaultLoginPageGeneratingFilter.class, this.loginPageGeneratingFilter);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void configure(final H http) throws Exception {
    final ExceptionHandlingConfigurer<?> exceptionConf = http.getConfigurer(ExceptionHandlingConfigurer.class);
    //final AuthenticationEntryPoint authenticationEntryPoint = exceptionConf != null ?
    // exceptionConf.getAuthenticationEntryPoint() : null;

    if (this.loginPageGeneratingFilter.isEnabled() && this.useDefaultLoginPage) { // authenticationEntryPoint == null
      this.loginPageGeneratingFilter.setLoginPageUrl(this.loginPage);
      this.configure(exceptionConf);
      this.loginPageGeneratingFilter = postProcess(this.loginPageGeneratingFilter);
      http.addFilter(this.loginPageGeneratingFilter);
    }
  }

  @SuppressWarnings("unchecked")
  private void configure(final ExceptionHandlingConfigurer<?> exceptionHandling) {
    if (exceptionHandling == null) {
      return;
    }
    // using DelegatingAuthenticationEntryPoint
    // AbstractAuthenticationFilterConfigurer#registerDefaultAuthenticationEntryPoint()
    final Field field = findField(ExceptionHandlingConfigurer.class, "defaultEntryPointMappings", LinkedHashMap.class);
    makeAccessible(field);
    final Map<RequestMatcher, AuthenticationEntryPoint> defaultEntryPointMappings = //
      (Map<RequestMatcher, AuthenticationEntryPoint>) getField(field, exceptionHandling);

    for (final Entry<RequestMatcher, AuthenticationEntryPoint> entry : defaultEntryPointMappings.entrySet()) {
      final AuthenticationEntryPoint value = entry.getValue();
      if (isAssignableValue(LoginUrlAuthenticationEntryPoint.class, value)) {
        ///final LoginUrlAuthenticationEntryPoint current = (LoginUrlAuthenticationEntryPoint) value;
        final TemplateAuthenticationEntryPoint replacement = templateEntryPoint( //
          this.loginPage, this.exceptionResolver, this.resolvedErrorCookie);
        entry.setValue(replacement);
      }
    }
  }
}
