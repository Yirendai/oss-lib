package com.yirendai.oss.lib.common;

import static com.google.common.collect.Lists.newArrayList;
import static com.yirendai.oss.lib.common.RequestUtlis.findWrapper;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.parseMediaType;

import com.google.common.collect.ImmutableSet;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

/**
 * build curl from request
 *
 * <p>
 * Created by zhanghaolun on 16/7/4.
 * </p>
 */
@Slf4j
public abstract class CurlUtils {

  private CurlUtils() {
  }

  private static final Collection<String> RETAIN_HEADERS =
    ImmutableSet.copyOf(newArrayList(ACCEPT, CONTENT_TYPE, AUTHORIZATION));

  /**
   * 安置http请求, 生成curl命令.
   *
   * @param request {@link HttpServletRequest}
   * @return curl command
   */
  public static String curl(final HttpServletRequest request) {
    final String result;
    if (request != null) {
      final MediaType contentType =
        isNotBlank(request.getContentType()) ? parseMediaType(request.getContentType()) : ALL;
      final String headers = curlHeaders(request);
      final String parameters = curlParameters(request);

      final StringBuilder curl = new StringBuilder("curl ").append(headers).append(" ")
        .append("-X ").append(request.getMethod()).append(" ");
      if (APPLICATION_JSON.includes(contentType) || APPLICATION_XML.includes(contentType)) {
        curl.append("--data '").append(curlBody(request)).append("' ");
      } else if (APPLICATION_FORM_URLENCODED == contentType) {
        curl.append("--data '").append(parameters).append("' ");
      } else if (isNotBlank(parameters)) {
        curl.append('?').append(parameters).append(' ');

      }
      curl.append(request.getRequestURL());
      result = curl.toString();
    } else {
      result = "";
    }
    return result;
  }

  static String curlHeaders(final HttpServletRequest request) {
    @SuppressWarnings("rawtypes")
    final Enumeration headerNames = request.getHeaderNames();
    final StringBuilder hBuilder = new StringBuilder();
    while (headerNames.hasMoreElements()) {
      final String name = (String) headerNames.nextElement();
      final String value = request.getHeader(name);
      if (RETAIN_HEADERS.contains(name)) {
        hBuilder.append("-H '").append(name).append(": ").append(value).append("' ");
      }
    }
    return hBuilder.toString();
  }

  @SneakyThrows
  static String curlParameters(final HttpServletRequest request) {
    @SuppressWarnings("rawtypes")
    final Enumeration parameterNames = request.getParameterNames();
    final StringBuilder pBuilder = new StringBuilder();
    while (parameterNames.hasMoreElements()) {
      final String name = (String) parameterNames.nextElement();
      final String value = request.getParameter(name);
      pBuilder //
        .append('&') //
        .append(name) //
        .append('=') //
        .append(CodecUtils.urlEncode(value));
    }
    return pBuilder.length() > 0 ? pBuilder.substring(1) : "";
  }

  @SneakyThrows
  public static String curlBody(final HttpServletRequest request) {
    final Charset charset = RequestUtlis.findCharset(request);
    try {
      // read raw inputStream first. (may be has not been read, for example 404)
      final String raw = StreamUtils.copyToString(request.getInputStream(), charset);
      final String result;
      if (isBlank(raw)) { // if no content in raw inputStream, 那应该是读过了, try to read cached.
        final ContentCachingRequestWrapper wrapper = findWrapper(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
          result = new String(((ContentCachingRequestWrapper) request).getContentAsByteArray(), charset);
        } else {
          result = "";
        }
      } else {
        result = raw;
      }
      return result;
    } catch (final IOException ex) {
      log.warn("error reading request body.", ex);
    }
    return "";
  }
}
