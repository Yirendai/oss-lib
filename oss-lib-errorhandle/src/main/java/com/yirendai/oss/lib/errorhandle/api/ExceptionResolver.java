package com.yirendai.oss.lib.errorhandle.api;

import org.springframework.web.context.request.RequestAttributes;

import javax.servlet.http.HttpServletRequest;

public interface ExceptionResolver<T extends Throwable> {

  String COMPOSITE_EXCEPTION_RESOLVER = "compositeExceptionResolver";

  ResolvedError resolve(HttpServletRequest request, T throwable);

  ResolvedError resolve(RequestAttributes requestAttributes, T throwable);
}
