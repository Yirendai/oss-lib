package com.yirendai.oss.lib.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.yirendai.oss.lib.common.Jackson2Configurator.BuildinJackson2Configurators;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.env.PropertyResolver;

import java.io.IOException;

@Slf4j
public class SpringSecurityJackson2Module extends SimpleModule {

  private static final long serialVersionUID = 1L;

  public SpringSecurityJackson2Module() {
    super();
    try {
      // TODO remove all SimpleGrantedAuthority.
      Class.forName("org.springframework.security.core.GrantedAuthority");
      addSerializer(org.springframework.security.core.GrantedAuthority.class, new GrantedAuthoritySerializer());
      addDeserializer(org.springframework.security.core.GrantedAuthority.class, new GrantedAuthorityDeserializer());
      addSerializer(org.springframework.security.core.authority.SimpleGrantedAuthority.class, //
        new GrantedAuthoritySerializer());
      addDeserializer(org.springframework.security.core.authority.SimpleGrantedAuthority.class, //
        new GrantedAuthorityDeserializer());
    } catch (final ClassNotFoundException ignored) {
      log.debug("class not found, skip SpringSecurityJackson2Module", ignored);
    }
  }

  /**
   * jackson2 module for spring security (
   * {@link org.springframework.security.core.GrantedAuthority},
   * {@link org.springframework.security.core.authority.SimpleGrantedAuthority}
   * ).
   *
   * @author zhanghaolun
   */
  public enum Jackson2SpringSecurityConfigurator implements Jackson2Configurator<BuildinJackson2Configurators> {
    JACKSON2_SPRINGSECURITY_CONFIGURATOR {
      @Override
      public <M extends ObjectMapper> M config(final PropertyResolver propertyResolver, final M mapper) {
        mapper.registerModule(new SpringSecurityJackson2Module());
        return mapper;
      }
    };

    @Override
    public abstract <M extends ObjectMapper> M config(PropertyResolver propertyResolver, M mapper);
  }

  public static class GrantedAuthoritySerializer //
    extends JsonSerializer<org.springframework.security.core.GrantedAuthority> {

    @Override
    public void serialize( //
      final org.springframework.security.core.GrantedAuthority value, //
      final JsonGenerator jsonGenerator, //
      final SerializerProvider provider //
    ) throws IOException {
      jsonGenerator.writeString(value.toString());
    }
  }

  public static class GrantedAuthorityDeserializer //
    extends JsonDeserializer<org.springframework.security.core.authority.SimpleGrantedAuthority> {

    @Override
    public org.springframework.security.core.authority.SimpleGrantedAuthority deserialize( //
      final JsonParser jsonParser, //
      final DeserializationContext deserializationContext //
    ) throws IOException {
      final org.springframework.security.core.authority.SimpleGrantedAuthority grantedAuthority;
      final JsonToken currentToken = jsonParser.getCurrentToken();

      if (JsonToken.VALUE_STRING.equals(currentToken)) {
        try {
          final String grantedAuthorityString = jsonParser.getText().trim();
          grantedAuthority = new org.springframework.security.core.authority.SimpleGrantedAuthority( //
            grantedAuthorityString);
          return grantedAuthority;
        } catch (final Exception translated) {
          throw new Jackson2Utils.RuntimeJsonProcessingException("couldn't parse GrantedAuthority", translated);
        }
      } else {
        throw new IllegalStateException();
      }
    }
  }
}
