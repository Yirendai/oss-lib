package com.yirendai.oss.lib.security.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yirendai.oss.lib.common.Jackson2Utils;

import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

/**
 * Created by Meifans on 17/1/9.
 */
@Slf4j
public class GenericUserJacksonTest {

  private JacksonTester<GenericUser> json;

  @Before
  public void setup() {
    final ObjectMapper objectMapper = Jackson2Utils.setupObjectMapper(null, new ObjectMapper());
    JacksonTester.initFields(this, objectMapper);
  }

  @Test
  public void testGenericUserSerialize() throws IOException {
    new GenericUserJsonTester(this.json).testGenericUserSerialize();
  }

  @Test
  public void testGenericUserDeSerialize() throws IOException {
    new GenericUserJsonTester(this.json).testGenericUserDeSerialize();
  }
}
