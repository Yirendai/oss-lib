package com.yirendai.oss.lib.errorhandle.api;

import static com.yirendai.oss.lib.errorhandle.api.ResolvedError.HEADER_RESOLVED_ERROR;
import static com.yirendai.oss.lib.errorhandle.api.ResolvedErrorTestScenario.resolvedErrorScenario;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yirendai.oss.lib.common.Jackson2Utils;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.Map;

/**
 * Created by haolun on 17/1/5.
 */
public class ResolvedErrorTest {

  private ObjectMapper objectMapper;

  @Before
  public void setUp() {
    this.objectMapper = Jackson2Utils.setupObjectMapper(null, new ObjectMapper());
  }

  @Test
  public void testHttpStatus() throws IOException {
    final int expectedStatus = 404;
    final ResolvedError resolvedError = ResolvedError.resolvedErrorBuilder() //
      .status(expectedStatus) //
      .build();
    assertEquals(expectedStatus, resolvedError.getHttpStatus().value());
  }

  @Test
  public void testNewHttpHeaders() {
    final HttpHeaders headers = ResolvedError.newHttpHeaders();

    assertTrue("headers not contain RESOLVED-ERROR", //
      headers.containsKey(HEADER_RESOLVED_ERROR));
    assertEquals("headers not contain RESOLVED-ERROR", //
      headers.get(HEADER_RESOLVED_ERROR).get(0), HEADER_RESOLVED_ERROR);
  }

  @Test
  public void testEraseTraces() {
    final ResolvedError resolvedError = ResolvedErrorTestScenario.resolvedErrorScenario();
    assertNotNull(resolvedError.getTracks());
    assertTrue(isNotBlank(resolvedError.getTrace()));

    resolvedError.eraseTraces();

    assertTrue(resolvedError.getTracks() == null);
    assertTrue(resolvedError.getTrace() == null);
  }

  @Test
  public void testPathPrepend() {
    final String pathToPrepend = "track0";
    final ResolvedError resolvedError = ResolvedErrorTestScenario.resolvedErrorScenario();
    final int tracksSize = resolvedError.getTracks().length;
    assertFalse(ArrayUtils.contains(resolvedError.getTracks(), pathToPrepend));
    resolvedError.trackPrepend(pathToPrepend);
    assertEquals(tracksSize + 1, resolvedError.getTracks().length);
    assertTrue(ArrayUtils.contains(resolvedError.getTracks(), pathToPrepend));
    assertEquals(pathToPrepend, resolvedError.getTracks()[0]);
  }

  @Test
  public void testFromErrorAttributes() {
    final ResolvedError expected = resolvedErrorScenario();
    @SuppressWarnings("unchecked")
    final Map<String, Object> errors = expected.toErrorAttributes();
    final ResolvedError actual = ResolvedError.fromErrorAttributes(errors);

    assertEquals("object not equal", expected, actual);
  }

  @Test
  public void testToErrorAttributes() {
    final ResolvedError expected = resolvedErrorScenario();
    final Map<String, Object> result = expected.toErrorAttributes();
    final ResolvedError actual = this.objectMapper.convertValue(result, ResolvedError.class);

    assertTrue("object not equal", actual.equals(expected));
  }
}
