package com.yirendai.oss.boot.autoconfigure;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.Optional;

/**
 * Created by zhanghaolun on 16/11/5.
 */
public class AppPropertiesTest {

  @Test
  public void testProfilePattern() {
    assertTrue(AppProperties.PROFILE_PATTERN.matcher(".env").matches());
    assertTrue(AppProperties.PROFILE_PATTERN.matcher("development.env").matches());
    assertFalse(AppProperties.PROFILE_PATTERN.matcher("development+.env").matches());
    assertFalse(AppProperties.PROFILE_PATTERN.matcher("development-env").matches());
  }

  @Test
  public void testFindEnv() {
    final MockEnvironment environment = new MockEnvironment();
    environment.setActiveProfiles(AppProperties.DEVELOPMENT_ENV, "someProfile");

    final Optional<String> found = AppProperties.findEnv(environment);
    assertTrue(found.isPresent());
    Assert.assertEquals(AppProperties.DEVELOPMENT, found.get());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFindEnvException() {
    final MockEnvironment environment = new MockEnvironment();
    environment.setActiveProfiles(AppProperties.DEVELOPMENT_ENV, AppProperties.PRODUCTION_ENV, "someProfile");

    AppProperties.findEnv(environment);
  }
}
