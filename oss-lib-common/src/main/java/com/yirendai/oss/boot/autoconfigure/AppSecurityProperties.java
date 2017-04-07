package com.yirendai.oss.boot.autoconfigure;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.yirendai.oss.lib.common.crypto.KeyExpression;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

@SuppressWarnings({"PMD.ImmutableField", "PMD.SingularField"})
@Data
public class AppSecurityProperties {

  public static final Boolean DEFAULT_APP_SECURITY_ENABLED = TRUE;
  public static final String APP_SECURITY = "app.security";
  public static final String ENCRYPTED_FIELD_PREFIX = "rsa:"; // TODO reanme "rsa"

  private KeyExpression cookieKey;
  private String defaultTestUser;
  private Boolean enabled;
  private KeyExpression jwtKey;
  private String permited;

  // ----- form -----
  private String authEntryPoint;
  private String authFailureHandler;
  /**
   * restful application: 'restful'.
   * template application: '${redirectUrl}'.
   */
  private String authSucessHandler;

  @Value("${security.basePath:/auth")
  private String basePath;

  private KeyExpression loginKey;
  @Value("${security.loginPublicKeyUrl:/login/publicKey")
  private String loginPublicKeyUrl;
  @Value("${security.loginPage:/login}")
  private String loginPage;
  @Value("${security.loginProcessingUrl:/login}")
  private String loginProcessingUrl;
  @Value("${security.logoutUrl:/logout}")
  private String logoutUrl;
  private Boolean verifyCode;
  // ----- form -----

  public AppSecurityProperties() {
    this.cookieKey = new KeyExpression();
    this.defaultTestUser = "";
    this.enabled = DEFAULT_APP_SECURITY_ENABLED;
    this.jwtKey = new KeyExpression();
    this.permited = "";

    // ----- form -----
    this.authEntryPoint = "";
    this.authFailureHandler = "";
    this.authSucessHandler = "";

    this.loginKey = new KeyExpression();
    this.loginPublicKeyUrl = "/login/publicKey";
    this.loginPage = "/login";
    this.loginProcessingUrl = "/login";
    this.logoutUrl = "/logout";
    this.verifyCode = FALSE;
    this.basePath = "/auth";
    // ----- form -----
  }

  /**
   * login endpoint. TODO remove this
   *
   * @return login url without leading '/'
   */
  public String getLoginEndpoint() {
    return this.basePath + this.loginProcessingUrl.substring(1);
  }

  String getDefaultTestUser() {
    return this.defaultTestUser;
  }

  public Boolean getEnabled() {
    return this.enabled;
  }

  public String getAuthEntryPoint() {
    return this.authEntryPoint;
  }

  public String getAuthFailureHandler() {
    return this.authFailureHandler;
  }

  public String getAuthSucessHandler() {
    return this.authSucessHandler;
  }

  public KeyExpression getLoginKey() {
    return this.loginKey;
  }

  public String getLoginPage() {
    return "/".equals(this.basePath) ? this.loginPage : this.basePath + this.loginPage;
  }

  public String getLoginPublicKeyUrl() {
    return "/".equals(this.basePath) ? this.loginPublicKeyUrl : this.basePath + this.loginPublicKeyUrl;
  }

  public String getLoginProcessingUrl() {
    return "/".equals(this.basePath) ? this.loginProcessingUrl : this.basePath + this.loginProcessingUrl;
  }

  public String getLogoutUrl() {
    return "/".equals(this.basePath) ? this.logoutUrl : this.basePath + this.logoutUrl;
  }

  public Boolean useDefaultLoginPage(final Environment environment) {
    return environment != null && isBlank(environment.getProperty("app.security.loginPage", ""));
  }

  public Boolean getVerifyCode() {
    return this.verifyCode;
  }
}
