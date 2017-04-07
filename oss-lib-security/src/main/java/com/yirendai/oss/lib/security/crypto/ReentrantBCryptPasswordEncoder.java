package com.yirendai.oss.lib.security.crypto;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;

/**
 * Created by zhanghaolun on 16/7/8.
 */
public class ReentrantBCryptPasswordEncoder implements PasswordEncoder {

  private static final char FIELD_SEPERATOR = '$';
  private final BCryptPasswordEncoder delegate;

  public ReentrantBCryptPasswordEncoder() {
    this(-1);
  }

  /**
   * constructor with strength.
   * 
   * @param strength the log rounds to use
   */
  public ReentrantBCryptPasswordEncoder(final int strength) {
    this(strength, null);
  }

  /**
   * constructor with strength and random.
   * 
   * @param strength the log rounds to use
   * @param random the secure random instance to use
   */
  public ReentrantBCryptPasswordEncoder(final int strength, final SecureRandom random) {
    this.delegate = new BCryptPasswordEncoder(strength, random);
  }

  public static Boolean isBCryptEncoded(final CharSequence encodedPassword) {
    return isNotBlank(encodedPassword) && encodedPassword.length() == 60
        && encodedPassword.charAt(0) == FIELD_SEPERATOR
        && encodedPassword.charAt(3) == FIELD_SEPERATOR
        && encodedPassword.charAt(6) == FIELD_SEPERATOR;
  }

  @Override
  public String encode(final CharSequence rawPassword) {
    return this.isEncoded(rawPassword) ? (rawPassword != null ? rawPassword.toString() : null)
        : this.delegate.encode(rawPassword);
  }

  @Override
  public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
    return this.delegate.matches(rawPassword, encodedPassword);
  }

  public boolean isEncoded(final CharSequence encodedPassword) {
    return isBCryptEncoded(encodedPassword);
  }
}
