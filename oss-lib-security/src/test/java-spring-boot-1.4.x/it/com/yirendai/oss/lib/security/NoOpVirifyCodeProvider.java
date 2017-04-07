package it.com.yirendai.oss.lib.security;

import static java.lang.Boolean.TRUE;

import com.yirendai.oss.lib.security.api.VerifyCodeProvider;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * Created by zhanghaolun on 16/7/15.
 */
@Component
@Slf4j
public class NoOpVirifyCodeProvider implements VerifyCodeProvider {

  public static final String URL_LOGIN_SEND_CODE = "/api/login/sendCode";

  public String getCodeUrl() {
    return URL_LOGIN_SEND_CODE;
  }

  @Override
  public Boolean match(final String key, final String code) {
    log.info("{} key: {}, code: {}", NoOpVirifyCodeProvider.class, key, code);
    return TRUE;
  }
}
