package com.yirendai.oss.lib.errorhandle.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yirendai.oss.lib.common.Jackson2Utils;

import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

/**
 * Created by Meifans on 17/1/10.
 */
@Slf4j
public class HttpHeaderJacksonTest {

  private JacksonTester<HttpHeader[]> json;

  @Before
  public void setUp() {
    final ObjectMapper objectMapper = Jackson2Utils.setupObjectMapper(null, new ObjectMapper());
    JacksonTester.initFields(this, objectMapper);
  }

  @Test
  public void testJsonSerialize() throws IOException {
    log.info("json:{}", this.json.write(HttpHeader.fromHttpHeaders(ResolvedError.newHttpHeaders())).getJson());
  }

}
