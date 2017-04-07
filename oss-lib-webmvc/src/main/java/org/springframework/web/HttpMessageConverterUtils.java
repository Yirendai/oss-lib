package org.springframework.web;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;

import com.google.common.collect.ImmutableMap;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.env.PropertyResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.util.ClassUtils;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.FixedContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;
import org.springframework.web.accept.ParameterContentNegotiationStrategy;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class HttpMessageConverterUtils {

  private HttpMessageConverterUtils() {
  }

  /**
   * Determine whether a JAXB binder is present on the classpath and can be loaded. Will return
   * <tt>false</tt> if either the {@link javax.xml.bind.Binder} or one of its dependencies is not
   * present or cannot be loaded.
   *
   * @param classLoader classLoader
   * @return isJaxb2Present
   */
  public static boolean isJaxb2Present(final ClassLoader classLoader) {
    return ClassUtils.isPresent("javax.xml.bind.Binder", classLoader);
  }

  /**
   * Determine whether Jackson 2.x is present on the classpath and can be loaded. Will return
   * <tt>false</tt> if either the {@code com.fasterxml.jackson.databind.ObjectMapper},
   * {@code com.fasterxml.jackson.core.JsonGenerator} or one of its dependencies is not present
   * or cannot be loaded.
   *
   * @param classLoader classLoader
   * @return isJackson2Present
   */
  public static boolean isJackson2Present(final ClassLoader classLoader) {
    return ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", classLoader)
      && ClassUtils.isPresent("com.fasterxml.jackson.core.JsonGenerator", classLoader);
  }

  /**
   * Determine whether Jackson 1.x is present on the classpath and can be loaded. Will return
   * <tt>false</tt> if either the {@code org.codehaus.jackson.map.ObjectMapper},
   * {@code org.codehaus.jackson.JsonGenerator} or one of its dependencies is not present or
   * cannot be loaded.
   *
   * @param classLoader classLoader
   * @return isJacksonPresent
   */
  @Deprecated
  public static boolean isJacksonPresent(final ClassLoader classLoader) {
    return ClassUtils.isPresent("org.codehaus.jackson.map.ObjectMapper", classLoader)
      && ClassUtils.isPresent("org.codehaus.jackson.JsonGenerator", classLoader);
  }

  /**
   * Returns default {@link HttpMessageConverter} instances, i.e.:
   *
   * <ul>
   * <li>{@linkplain ByteArrayHttpMessageConverter}</li>
   * <li>{@linkplain StringHttpMessageConverter}</li>
   * <li>{@linkplain ResourceHttpMessageConverter}</li>
   * <li>{@linkplain Jaxb2RootElementHttpMessageConverter} (when JAXB is present)</li>
   * <li>{@linkplain MappingJackson2HttpMessageConverter} (when Jackson 2.x is present)</li>
   * <li>{org.springframework.http.converter.json.MappingJacksonHttpMessageConverter}
   * (when Jackson 1.x is present and 2.x not)</li>
   * </ul>
   *
   * <p>
   * Note: It does not return all of the default converters defined in Spring, but just thus
   * usable for exception responses.
   * </p>
   *
   * @param propertyResolver propertyResolver
   * @param objectMapper     objectMapper
   * @return httpMessageConverters
   */
  public static List<HttpMessageConverter<?>> defaultHttpMessageConverters(
    final PropertyResolver propertyResolver,
    final Object objectMapper
  ) {
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    final List<HttpMessageConverter<?>> converters = new ArrayList<>();

    final StringHttpMessageConverter stringConverter =
      new StringHttpMessageConverter(Charset.forName("UTF-8"));
    stringConverter.setWriteAcceptCharset(false); // See SPR-7316

    converters.add(new ByteArrayHttpMessageConverter());
    converters.add(stringConverter);
    converters.add(new ResourceHttpMessageConverter());

    if (isJaxb2Present(classLoader)) {
      converters.add(new Jaxb2RootElementHttpMessageConverter());
    }
    if (isJackson2Present(classLoader)) {
      if (objectMapper == null) {
        converters.add(new MappingJackson2HttpMessageConverter( //
            com.yirendai.oss.lib.common.Jackson2Utils.setupObjectMapper( //
              propertyResolver, //
              Jackson2ObjectMapperBuilder.json().build()) //
          ) //
        );
      } else {
        converters.add(new MappingJackson2HttpMessageConverter( //
          (com.fasterxml.jackson.databind.ObjectMapper) objectMapper) //
        );
      }
    } else if (isJacksonPresent(classLoader)) {
      try {
        final String className = "org.springframework.http.converter.json.MappingJacksonHttpMessageConverter";
        final Class<?> clazz = Class.forName(className);
        converters.add((HttpMessageConverter<?>) clazz.newInstance());

      } catch (final ClassNotFoundException ex) {
        // Ignore it, this class is not available since Spring 4.1.0.
        log.trace("MappingJacksonHttpMessageConverter is not available since Spring 4.1.0.", ex);
      } catch (InstantiationException | IllegalAccessException ex) {
        throw new IllegalStateException(ex);
      }
    }
    return converters;
  }

  public static ContentNegotiationManager defaultContentNegotiationManager() {
    return defaultContentNegotiationManager(APPLICATION_JSON);
  }

  public static ContentNegotiationManager defaultContentNegotiationManager(final MediaType defaultContentType) {
    final ContentNegotiationManager contentNegotiationManager = new ContentNegotiationManager( //
      new HeaderContentNegotiationStrategy(), //
      new ParameterContentNegotiationStrategy( //
        ImmutableMap.of("json", APPLICATION_JSON, "xml", APPLICATION_XML) //
      ), //
      new FixedContentNegotiationStrategy(defaultContentType) //
    );
    return contentNegotiationManager;
  }
}
