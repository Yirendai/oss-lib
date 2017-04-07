package com.yirendai.oss.lib.security.crypto;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import lombok.extern.slf4j.Slf4j;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Created by zhanghaolun on 16/7/8.
 */
@Slf4j
public class PasswordEncoderTest {

  @Test
  public void testBCryptPasswordEncoder() {
    final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    final String rawPassword = "rootroot";
    final String first = encoder.encode(rawPassword);
    final String second = encoder.encode(rawPassword);
    log.info("rawPassword: {}, first: {}, second: {}", rawPassword, first, second);

    assertTrue(encoder.matches(rawPassword, first));
    assertTrue(encoder.matches(rawPassword, second));
    assertTrue(ReentrantBCryptPasswordEncoder.isBCryptEncoded(first));
    assertTrue(ReentrantBCryptPasswordEncoder.isBCryptEncoded(second));
    assertNotEquals(first, second);
    assertFalse(encoder.matches(rawPassword, encoder.encode(first)));

    IntStream.range(0, 10).forEach(i -> {
      final String raw = UUID.randomUUID().toString();
      assertFalse(ReentrantBCryptPasswordEncoder.isBCryptEncoded(raw));
      assertTrue(ReentrantBCryptPasswordEncoder.isBCryptEncoded(encoder.encode(raw)));
    });
  }
}
