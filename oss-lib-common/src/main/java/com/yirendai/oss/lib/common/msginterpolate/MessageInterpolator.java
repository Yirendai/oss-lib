package com.yirendai.oss.lib.common.msginterpolate;

import java.util.Map;

public interface MessageInterpolator {

  /**
   * Interpolates the message template using the given variables.
   *
   * @param template  The message to interpolate.
   * @param variables Map of variables that will be accessible for the template.
   * @return An interpolated message.
   */
  String interpolate(String template, Map<String, Object> variables);
}
