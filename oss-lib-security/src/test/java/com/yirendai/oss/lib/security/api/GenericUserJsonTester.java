package com.yirendai.oss.lib.security.api;

import static com.yirendai.oss.lib.common.Defaults.ISO8601;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.test.json.AbstractJsonMarshalTester;

import java.io.IOException;

/**
 * Created by Meifans on 17/1/5.
 */
@Slf4j
public class GenericUserJsonTester {

  private AbstractJsonMarshalTester<GenericUser> json;

  public GenericUserJsonTester(AbstractJsonMarshalTester<GenericUser> json) {
    this.json = json;
  }

  public void testGenericUserSerialize() throws IOException {
    final GenericUser scenario = GenericUserTestScenario.genericUserScenario();

    this.json.write(scenario).assertThat()
      .extractingJsonPathMapValue("properties").containsAllEntriesOf(scenario.getProperties());

    this.json.write(scenario).assertThat()
      .extractingJsonPathArrayValue("authorities").contains(
      scenario.getAuthorities().iterator().next().getAuthority());

    this.json.write(scenario).assertThat()
      .extractingJsonPathStringValue("timestamp").isEqualTo(ISO8601.print(scenario.getTimestamp()));
  }

  public void testGenericUserDeSerialize() throws IOException {
    this.json.read("/genericUserScenario.json").assertThat().isEqualTo(GenericUserTestScenario.genericUserScenario());
  }
}
