package com.yirendai.oss.lib.errorhandle.internal;

import static com.yirendai.oss.lib.common.RequestUtlis.findWrapper;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.OPTIONS;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 统一异常处理的日志追踪功能会拼写curl命令。
 * 其中应为body部分因为不能重复读取(ServletInputStream不支持)，故增加此类使得body内容可再次拿到
 * {@link com.yirendai.oss.lib.common.CurlUtils}
 * Created by sunday on 2016/10/25.
 */
//@WebFilter(urlPatterns = {"/*"})
public class ContentCachingRequestFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal( //
    final HttpServletRequest request, //
    final HttpServletResponse response, //
    final FilterChain filterChain //
  ) throws ServletException, IOException {
    final RequestMethod requestMethod = RequestMethod.valueOf(request.getMethod());

    final String contentType = request.getContentType();
    final Boolean interested = requestMethod != GET && requestMethod != OPTIONS && //
      contentType != null && (contentType.contains("xml") || contentType.contains("json"));

    if (interested) {
      final ContentCachingRequestWrapper found = findWrapper(request, ContentCachingRequestWrapper.class);
      if (found == null) {
        filterChain.doFilter(new ContentCachingRequestWrapper(request), response);
      } else {
        filterChain.doFilter(request, response);
      }
    } else {
      filterChain.doFilter(request, response);
    }
  }
}
