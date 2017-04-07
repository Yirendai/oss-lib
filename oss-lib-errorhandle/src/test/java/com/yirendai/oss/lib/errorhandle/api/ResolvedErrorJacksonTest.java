package com.yirendai.oss.lib.errorhandle.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yirendai.oss.lib.common.Jackson2Utils;

import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Created by Meifans on 17/1/3.
 */
@Slf4j
public class ResolvedErrorJacksonTest {

  private JacksonTester<ResolvedError> jsonMarshalTester;

  @Before
  public void setUp() {
    final ObjectMapper objectMapper = Jackson2Utils.setupObjectMapper(null, new ObjectMapper());
    JacksonTester.initFields(this, objectMapper);
  }

  @SuppressFBWarnings("NP_UNWRITTEN_FIELD")
  @Test
  public void testJsonSerialize() throws IOException {
    new ResolvedErrorJsonTester(this.jsonMarshalTester).testSerialize();
  }

  @Test
  public void testDeserializeResolvedError() throws IOException {
    new ResolvedErrorJsonTester(this.jsonMarshalTester).testDeserialize();
  }
}
