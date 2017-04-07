package com.yirendai.oss.lib.errorhandle.api;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.json.GsonTester;

import java.io.IOException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * see: https://github.com/spring-projects/spring-boot/blob/master/spring-boot-autoconfigure
 * /src/main/java/org/springframework/boot/autoconfigure/web/GsonHttpMessageConvertersConfiguration.java
 */
public class ResolvedErrorGsonTest {

  private GsonTester<ResolvedError> jsonMarshalTester;

  @Before
  public void setup() {
    GsonTester.initFields(this, new Gson());
  }

  @SuppressFBWarnings({"NP_UNWRITTEN_FIELD", "UWF_UNWRITTEN_FIELD"})
  @Test
  public void testSerializeResolvedError() throws IOException {
    new ResolvedErrorJsonTester(this.jsonMarshalTester).testSerialize();
  }

  @Test
  public void testDeserializeResolvedError() throws IOException {
    new ResolvedErrorJsonTester(this.jsonMarshalTester).testDeserialize();
  }
}
