package org.springframework.http.converter;

import static com.google.common.base.Charsets.ISO_8859_1;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static org.springframework.http.MediaType.parseMediaType;

import com.google.common.collect.ImmutableList;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collection;
import java.util.List;

@Slf4j
public class Utf8StringHttpMessageConverter extends StringHttpMessageConverter {

  public static final MediaType TEXT_PLAIN_UTF8 = parseMediaType("text/plain;charset=UTF-8");

  private final List<Charset> availableCharsets;

  /**
   * build a UTF-8 string HTTP message converter.
   */
  public Utf8StringHttpMessageConverter() {
    super(UTF_8);
    final List<Charset> charsets = newLinkedList();
    charsets.add(UTF_8);
    charsets.add(ISO_8859_1);
    try {
      charsets.add(Charset.forName("GBK"));
    } catch (final UnsupportedCharsetException ignored) {
      // no-op
      log.trace("unsupported charset GBK.", ignored);
    }
    try {
      charsets.add(Charset.forName("GB2312"));
    } catch (final UnsupportedCharsetException ignored) {
      // no-op
      log.trace("unsupported charset GB2312.", ignored);
    }
    try {
      charsets.add(Charset.forName("GB18030"));
    } catch (final UnsupportedCharsetException ignored) {
      // no-op
      log.trace("unsupported charset GB18030.", ignored);
    }
    this.availableCharsets = ImmutableList.copyOf(charsets);
    this.addSupportedMediaTypes(TEXT_PLAIN_UTF8);
    this.setWriteAcceptCharset(true);
  }

  /**
   * addSupportedMediaTypes.
   *
   * @param mediaType mediaType to add
   * @return converter
   */
  public final Utf8StringHttpMessageConverter addSupportedMediaTypes(final MediaType mediaType) {
    final List<MediaType> current = this.getSupportedMediaTypes();
    final Collection<MediaType> supportedMediaTypes = newLinkedHashSet();
    if (current != null) {
      supportedMediaTypes.addAll(current);
    }
    supportedMediaTypes.add(mediaType);
    this.setSupportedMediaTypes(newArrayList(supportedMediaTypes));
    return this;
  }

  @Override
  protected List<Charset> getAcceptedCharsets() {
    return this.availableCharsets;
  }
}
