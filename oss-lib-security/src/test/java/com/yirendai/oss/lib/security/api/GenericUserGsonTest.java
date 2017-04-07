package com.yirendai.oss.lib.security.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.yirendai.oss.lib.common.GsonUtils;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.json.GsonTester;

import java.io.IOException;

/**
 * Created by Meifans on 17/1/10.
 */
public class GenericUserGsonTest {
  
  private GsonTester<GenericUser> jsonMarshalTester;

  @Before
  public void setup() {
    final Gson gson = GsonUtils.setup(Converters.registerDateTime(new GsonBuilder()));
    GsonTester.initFields(this, gson);
  }

  @Test
  public void testGenericUserSerialize() throws IOException {
    new GenericUserJsonTester(this.jsonMarshalTester).testGenericUserSerialize();
  }

  @Test
  public void testGenericUserDeSerialize() throws IOException {
    new GenericUserJsonTester(this.jsonMarshalTester).testGenericUserDeSerialize();
  }
}
