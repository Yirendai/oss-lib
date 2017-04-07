package com.yirendai.oss.lib.security.internal.template;

import static com.yirendai.oss.boot.autoconfigure.AppSecurityProperties.ENCRYPTED_FIELD_PREFIX;
import static com.yirendai.oss.lib.security.internal.template.SmartRedirectStrategy.PARAM_REDIRECT;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.yirendai.oss.lib.common.CodecUtils;

import lombok.SneakyThrows;

import org.apache.commons.io.IOUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.security.web.csrf.CsrfToken;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * see: {@link DefaultLoginPageGeneratingFilter}
 * Created by zhanghaolun on 16/11/9.
 */
public class EncryptDefaultLoginPageGeneratingFilter extends DefaultLoginPageGeneratingFilter {

  private String loginPageUrl;
  private String logoutSuccessUrl;
  private String failureUrl;
  private boolean formLoginEnabled;
  private boolean openIdEnabled;
  private String authenticationUrl;
  private String usernameParameter;
  private String passwordParameter;
  private String rememberMeParameter;
  private String openIDauthenticationUrl;
  private String openIDusernameParameter;
  private String openIDrememberMeParameter;

  private String jsencrypt;
  private String rsaPublicKey;

  public EncryptDefaultLoginPageGeneratingFilter() {
    this.jsencrypt = this.getJsencrypt();
  }

  public EncryptDefaultLoginPageGeneratingFilter(final AbstractAuthenticationProcessingFilter filter) {
    if (filter instanceof UsernamePasswordAuthenticationFilter) {
      this.childInit((UsernamePasswordAuthenticationFilter) filter, null);
    } else {
      this.childInit(null, filter);
    }
  }

  public EncryptDefaultLoginPageGeneratingFilter( //
    final UsernamePasswordAuthenticationFilter authFilter, //
    final AbstractAuthenticationProcessingFilter openIdFilter //
  ) {
    this.childInit(authFilter, openIdFilter);
  }

  private void childInit( //
    final UsernamePasswordAuthenticationFilter authFilter, //
    final AbstractAuthenticationProcessingFilter openIdFilter //
  ) {
    this.loginPageUrl = this.loginPageUrl != null ? this.loginPageUrl : DEFAULT_LOGIN_PAGE_URL;
    this.logoutSuccessUrl = this.loginPageUrl + "?logout";
    this.failureUrl = this.loginPageUrl + "?" + ERROR_PARAMETER_NAME;
    if (authFilter != null) {
      this.formLoginEnabled = true;
      this.usernameParameter = authFilter.getUsernameParameter();
      this.passwordParameter = authFilter.getPasswordParameter();

      if (authFilter.getRememberMeServices() instanceof AbstractRememberMeServices) {
        this.rememberMeParameter = ((AbstractRememberMeServices) authFilter
          .getRememberMeServices()).getParameter();
      }
    }

    if (openIdFilter != null) {
      this.openIdEnabled = true;
      this.openIDusernameParameter = "openid_identifier";

      if (openIdFilter.getRememberMeServices() instanceof AbstractRememberMeServices) {
        this.openIDrememberMeParameter = ((AbstractRememberMeServices) openIdFilter
          .getRememberMeServices()).getParameter();
      }
    }

    this.jsencrypt = this.getJsencrypt();
  }

  @Override
  public boolean isEnabled() {
    return this.formLoginEnabled || this.openIdEnabled;
  }

  @Override
  public void setLogoutSuccessUrl(final String logoutSuccessUrl) {
    this.logoutSuccessUrl = logoutSuccessUrl;
  }

  @Override
  public String getLoginPageUrl() {
    return this.loginPageUrl;
  }

  @Override
  public void setLoginPageUrl(final String loginPageUrl) {
    this.loginPageUrl = loginPageUrl;
  }

  @Override
  public void setFailureUrl(final String failureUrl) {
    this.failureUrl = failureUrl;
  }

  @Override
  public void setFormLoginEnabled(final boolean formLoginEnabled) {
    this.formLoginEnabled = formLoginEnabled;
  }

  @Override
  public void setOpenIdEnabled(final boolean openIdEnabled) {
    this.openIdEnabled = openIdEnabled;
  }

  @Override
  public void setAuthenticationUrl(final String authenticationUrl) {
    this.authenticationUrl = authenticationUrl;
  }

  @Override
  public void setUsernameParameter(final String usernameParameter) {
    this.usernameParameter = usernameParameter;
  }

  @Override
  public void setPasswordParameter(final String passwordParameter) {
    this.passwordParameter = passwordParameter;
  }

  @Override
  public void setRememberMeParameter(final String rememberMeParameter) {
    this.rememberMeParameter = rememberMeParameter;
    this.openIDrememberMeParameter = rememberMeParameter;
  }

  @Override
  public void setOpenIDauthenticationUrl(final String openIDauthenticationUrl) {
    this.openIDauthenticationUrl = openIDauthenticationUrl;
  }

  @Override
  public void setOpenIDusernameParameter(final String openIDusernameParameter) {
    this.openIDusernameParameter = openIDusernameParameter;
  }

  @SneakyThrows
  public String getJsencrypt() {
    try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("public/js/jsencrypt-2.3.1.min.js")) {
      return IOUtils.toString(is, StandardCharsets.UTF_8);
    }
  }

  public void setRsaPublicKey(final String rsaPublicKey) {
    this.rsaPublicKey = rsaPublicKey;
  }

  @Override
  public void doFilter( //
    final ServletRequest req, //
    final ServletResponse res, //
    final FilterChain chain //
  ) throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;

    boolean loginError = childIsErrorPage(request);
    boolean logoutSuccess = childIsLogoutSuccess(request);
    if (childIsLoginUrlRequest(request) || loginError || logoutSuccess) {
      String loginPageHtml = childGenerateLoginPageHtml(request, loginError,
        logoutSuccess);
      response.setContentType("text/html;charset=UTF-8");
      response.setContentLength(loginPageHtml.length());
      response.getWriter().write(loginPageHtml);

      return;
    }

    chain.doFilter(request, response);
  }

  private String childGenerateLoginPageHtml(HttpServletRequest request, boolean loginError,
    boolean logoutSuccess) {
    String errorMsg = "none";

    if (loginError) {
      HttpSession session = request.getSession(false);

      if (session != null) {
        AuthenticationException ex = (AuthenticationException) session
          .getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        errorMsg = ex != null ? ex.getMessage() : "none";
      }
    }

    StringBuilder sb = new StringBuilder();

    sb.append("<html>") //
      .append("<head>") //
      .append("<title>Login Page</title>") //
      .append("</head>");

    if (this.formLoginEnabled) {
      sb.append("<body onload='document.f.").append(this.usernameParameter)
        .append(".focus();'>\n");
    }

    if (loginError) {
      sb.append("<p><font color='red'>Your login attempt was not successful, try again.<br/><br/>Reason: ");
      sb.append(errorMsg);
      sb.append("</font></p>");
    }

    if (logoutSuccess) {
      sb.append("<p><font color='green'>You have been logged out</font></p>");
    }

    if (this.formLoginEnabled) {
      final String redirectParam = request.getParameter(PARAM_REDIRECT);
      final String baseUri = request.getContextPath() + this.authenticationUrl;
      final String action = isNotBlank(redirectParam) ?  //
        baseUri + "?" + PARAM_REDIRECT + "=" + CodecUtils.urlEncode(redirectParam) : baseUri;
      sb.append("<h3>Login with Username and Password.</h3>");
      sb.append("<form name='f' action='").append(action) //
        .append("' method='POST' onsubmit='return onSubmit();'>\n");
      sb.append("<table>\n");
      sb.append("    <tr><td>User:</td><td><input type='text' name='");
      sb.append(this.usernameParameter).append("' value='").append("'></td></tr>\n");
      sb.append("    <tr><td>Password:</td><td><input type='password' name='")
        .append(this.passwordParameter).append("'/></td></tr>\n");

      if (this.rememberMeParameter != null) {
        sb.append("    <tr><td><input type='checkbox' name='")
          .append(this.rememberMeParameter)
          .append("'/></td><td>Remember me on this computer.</td></tr>\n");
      }

      sb.append("    <tr><td colspan='2'>\n")
        .append("        <input onclick=\"onClick()\" name=\"submit\" type=\"submit\" value=\"Login\"/>\n")
        .append("    </td></tr>\n");
      childRenderHiddenInputs(sb, request);
      sb.append("</table>\n");
      sb.append("</form>");
    }

    if (this.openIdEnabled) {
      sb.append("<h3>Login with OpenID Identity</h3>");
      sb.append("<form name='oidf' action='").append(request.getContextPath())
        .append(this.openIDauthenticationUrl).append("' method='POST'>\n");
      sb.append("<table>\n");
      sb.append("    <tr><td>Identity:</td><td><input type='text' size='30' name='");
      sb.append(this.openIDusernameParameter).append("'/></td></tr>\n");

      if (this.openIDrememberMeParameter != null) {
        sb.append("    <tr><td><input type='checkbox' name='")
          .append(this.openIDrememberMeParameter)
          .append("'></td><td>Remember me on this computer.</td></tr>\n");
      }

      sb.append("    <tr><td colspan='2'>\n")
        .append("        <input name=\"submit\" type=\"submit\" value=\"Login\" />\n")
        .append("    </td></tr>\n");
      sb.append("</table>\n");
      childRenderHiddenInputs(sb, request);
      sb.append("</form>");
    }

    // RSA password encrypt
    if (this.jsencrypt != null) {
      sb.append("<script type='text/javascript'>\n").append(this.jsencrypt).append("\n</script>\n");
    } else {
      sb.append("<script type='text/javascript' src='js/jsencrypt-2.3.1.min.js'></script>"); //
    }
    sb.append("<script type='text/javascript'>\n") //
      .append("    function onClick() {\n") //
      .append("        var input = document.getElementsByName(\"").append(this.passwordParameter).append("\")[0];") //
      .append("\n") //
      .append("        var plaintext = input.value;\n") //
      .append("        if (! plaintext) {return false;}\n") //
      .append("        if (plaintext.startsWith('").append(ENCRYPTED_FIELD_PREFIX).append("')) {return true;}\n\n") //
      .append("        console.log('plaintext:' + plaintext);\n") //
      .append("        var rsaPublicKey='").append(isNotBlank(this.rsaPublicKey) ? this.rsaPublicKey : "") //
      .append("';\n") //
      .append("        if (! rsaPublicKey) {return true;}\n") //
      .append("        var encrypt = new JSEncrypt();\n") //
      .append("        encrypt.setPublicKey(rsaPublicKey);\n") //
      .append("        var encrypted = encrypt.encrypt(plaintext);\n") //
      .append("        var result = '").append(ENCRYPTED_FIELD_PREFIX).append("' + encrypted;\n") //
      .append("        input.value = result;\n")
      .append("        console.log('result:' + input.value);\n") //
      .append("        if (encrypted) {return true;} else {return false;}\n") //
      .append("    }\n") //
      .append("    function onSubmit() {return true;}\n") //
      .append("</script>\n");
    sb.append("</body></html>");

    return sb.toString();
  }

  private void childRenderHiddenInputs(StringBuilder sb, HttpServletRequest request) {
    final CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    if (token != null) {
      sb.append("    <input name=\"").append(token.getParameterName()) //
        .append("\" type=\"hidden\" value=\"").append(token.getToken()) //
        .append("\" />\n");
    }
  }

  private boolean childIsLogoutSuccess(HttpServletRequest request) {
    return this.logoutSuccessUrl != null && childMatches(request, this.logoutSuccessUrl);
  }

  private boolean childIsLoginUrlRequest(HttpServletRequest request) {
    final String url = request.getQueryString() != null ? //
      this.loginPageUrl + "?" + request.getQueryString() : this.loginPageUrl;
    return childMatches(request, url);
  }

  private boolean childIsErrorPage(HttpServletRequest request) {
    return childMatches(request, this.failureUrl);
  }

  private boolean childMatches(final HttpServletRequest request, final String url) {
    if (!"GET".equals(request.getMethod()) || url == null) {
      return false;
    }
    String uri = request.getRequestURI();
    int pathParamIndex = uri.indexOf(';');

    if (pathParamIndex > 0) {
      // strip everything after the first semi-colon
      uri = uri.substring(0, pathParamIndex);
    }

    if (request.getQueryString() != null) {
      uri += "?" + request.getQueryString();
    }

    if ("".equals(request.getContextPath())) {
      return uri.equals(url);
    }

    final String urlWithContextPath = request.getContextPath() + url;
    return uri.equals(urlWithContextPath);
  }
}
