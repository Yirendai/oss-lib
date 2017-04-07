package com.yirendai.oss.lib.errorhandle.api;

/**
 * Created by haolun on 17/1/5.
 */
public final class ResolvedErrorTestScenario {

  private ResolvedErrorTestScenario() {
  }

  public static ResolvedError resolvedErrorScenario() {
    return ResolvedError.resolvedErrorBuilder()
      .error("error")
      .errors(new ValidationError[]{})
      .exception("exception")
      .message("message")
      .path("path")
      .status(401)
      .timestamp(201701031643L)
      .trace("trace")
      //
      .datetime("datetime")
      .headers(HttpHeader.fromHttpHeaders(ResolvedError.newHttpHeaders()))
      .localizedMessage("localizedMessage")
      .tracks(new String[]{"track1", "track2"})
      .build();
  }
}
