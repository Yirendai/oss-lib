package com.yirendai.oss.lib.errorhandle.api;

import static com.yirendai.oss.lib.errorhandle.api.ResolvedErrorTestScenario.resolvedErrorScenario;
import static org.assertj.core.api.Assertions.assertThat;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.test.json.AbstractJsonMarshalTester;

import java.io.IOException;

/**
 * Created by haolun on 17/1/5.
 */
@Slf4j
public class ResolvedErrorJsonTester {

  private final AbstractJsonMarshalTester<ResolvedError> jsonMarshalTester;

  public ResolvedErrorJsonTester(final AbstractJsonMarshalTester<ResolvedError> jsonMarshalTester) {
    this.jsonMarshalTester = jsonMarshalTester;
  }

  public void testSerialize() throws IOException {
    final ResolvedError resolvedError = resolvedErrorScenario();

    log.info("json: {}", this.jsonMarshalTester.write(resolvedError).getJson());

    this.jsonMarshalTester.write(resolvedError).assertThat()
      .extractingJsonPathNumberValue("@.status") //
      .isEqualTo(resolvedError.getStatus());

    this.jsonMarshalTester.write(resolvedError).assertThat()
      .extractingJsonPathStringValue("@.error") //
      .isEqualTo(resolvedError.getError());

    this.jsonMarshalTester.write(resolvedError).assertThat() //
      .extractingJsonPathArrayValue("@.tracks") //
      .contains(resolvedError.getTracks()[0]) //
      .contains(resolvedError.getTracks()[1]);

    assertThat(this.jsonMarshalTester.write(resolvedError)).isEqualToJson("/resolvedErrorScenario.json");
  }

  public void testDeserialize() throws IOException {
    final ResolvedError resolvedError = resolvedErrorScenario();

    this.jsonMarshalTester.read("/resolvedErrorScenario.json").assertThat()
      .isEqualTo(resolvedError);
  }
}
