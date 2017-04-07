package com.yirendai.oss.lib.common;

import static com.google.common.base.Charsets.UTF_8;
import static com.yirendai.oss.lib.common.crypto.CryptoConstants.COLON;

import lombok.SneakyThrows;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.springframework.util.ClassUtils;

public abstract class BasicAuthUtils {

  public static final String BASIC_AUTH_HEADE_NAME = "Authorization";

  private static final String BAD_CREDENTIALS = "org.springframework.security.authentication.BadCredentialsException";
  private static final Boolean BAD_CREDENTIALS_PRESENT;

  private static final String MESSAGE_FAILED_TO_DECODE_TOKEN = "Failed to decode basic authentication token";
  private static final String MESSAGE_INVALID_TOKEN = "Invalid basic authentication token";

  static {
    BAD_CREDENTIALS_PRESENT = ClassUtils.isPresent(BAD_CREDENTIALS, BasicAuthUtils.class.getClassLoader());
  }

  private BasicAuthUtils() {
  }

  public static String basicAuthHeader(final String username, final String password) {
    final String authStr = username + COLON + password;
    return "Basic " + CodecUtils.encodeBase64(authStr.getBytes(UTF_8));
  }

  /**
   * Decodes the header into a username and password.
   * throws BadCredentialsException or IllegalArgumentException if
   * the Basic header is not present or is not valid Base64.
   *
   * @param header             header
   * @param credentialsCharset charset
   * @return [username, password]
   */
  @SneakyThrows
  public static String[] extractAndDecodeAuthHeader( //
    final String header, //
    final String credentialsCharset //
  ) {
    final String base64Token = header.substring(6);
    byte[] decoded;
    try {
      decoded = CodecUtils.decodeBase64(base64Token);
    } catch (final IllegalArgumentException cause) {
      throw BAD_CREDENTIALS_PRESENT ? //
        (RuntimeException) Class.forName(BAD_CREDENTIALS).getConstructor(String.class, Throwable.class) //
          .newInstance(MESSAGE_FAILED_TO_DECODE_TOKEN, cause) : //
        new IllegalArgumentException(MESSAGE_FAILED_TO_DECODE_TOKEN, cause);
    }

    final String token = new String(decoded, credentialsCharset);
    final int delim = token.indexOf(':');
    if (delim == -1) {
      if (!BAD_CREDENTIALS_PRESENT) {
        throw new IllegalArgumentException(MESSAGE_INVALID_TOKEN);
      } else {
        throw (RuntimeException) Class.forName(BAD_CREDENTIALS) //
          .getConstructor(String.class) //
          .newInstance(MESSAGE_INVALID_TOKEN);
      }
    }
    return new String[]{token.substring(0, delim), token.substring(delim + 1)};
  }

  /**
   * @deprecated remove this later.
   * @param username username
   * @param password password
   * @return credentialsProvider
   */
  @Deprecated
  public static CredentialsProvider basicCredentialsProvider(final String username, final String password) {
    final Credentials credentials = new UsernamePasswordCredentials(username, password);
    final CredentialsProvider provider = new BasicCredentialsProvider();
    provider.setCredentials(AuthScope.ANY, credentials);
    return provider;
  }
}
