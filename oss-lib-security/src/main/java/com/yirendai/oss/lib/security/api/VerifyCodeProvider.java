package com.yirendai.oss.lib.security.api;

/**
 * Application that uses verify code should provide a implementation of this interface.
 * Usually is a spring mvc controller.
 */
public interface VerifyCodeProvider {

  /**
   * url that send a sms code or display a CAPTCHA code.
   *
   * @return url
   */
  String getCodeUrl();

  /**
   * verify user input.
   *
   * @param uuid user's uuid
   * @param code the code user input
   * @return is match
   */
  Boolean match(String uuid, String code);
}
