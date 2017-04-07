package com.yirendai.oss.lib.errorhandle.mockmvc;


import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({ //
  DispatcherServletAutoConfiguration.class, //
  EmbeddedServletContainerAutoConfiguration.EmbeddedJetty.class, //
  EmbeddedServletContainerAutoConfiguration.class, //
  HttpMessageConvertersAutoConfiguration.class, //
  PropertyPlaceholderAutoConfiguration.class, //
  ServerPropertiesAutoConfiguration.class, //
  WebMvcAutoConfiguration.class})
public @interface MinimalWebConfiguration {
}
