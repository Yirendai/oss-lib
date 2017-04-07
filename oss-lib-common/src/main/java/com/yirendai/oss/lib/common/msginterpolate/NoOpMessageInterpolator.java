package com.yirendai.oss.lib.common.msginterpolate;

import java.util.Map;

/**
 * Implementation of the {@link MessageInterpolator} that does nothing, just returns the given
 * message template as-is.
 */
public class NoOpMessageInterpolator implements MessageInterpolator {

  @Override
  public String interpolate(final String template, final Map<String, Object> variables) {
    return template;
  }
}
