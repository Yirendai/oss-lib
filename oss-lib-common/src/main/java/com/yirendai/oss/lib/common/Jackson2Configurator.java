package com.yirendai.oss.lib.common;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.yirendai.oss.lib.common.Defaults.UTC_P8;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.parseBoolean;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertyResolver;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;

/**
 * Created by zhanghaolun on 16/7/28.
 */
public interface Jackson2Configurator<T extends Enum<T> & Jackson2Configurator<T>> {

  String JACKSON_JAXB_ENABLED = "jackson.jaxb.enabled";
  String XMLMAPPER_CLASSNAME = "com.fasterxml.jackson.dataformat.xml.XmlMapper";

  <M extends ObjectMapper> M config(PropertyResolver propertyResolver, M mapper);

  default Optional<Class<?>> findClass(final String className) {
    Class<?> classFound;
    try {
      classFound = Class.forName(className);
    } catch (final ClassNotFoundException ex) {
      LoggerFactory.getLogger(Jackson2Configurator.class).debug("{} not found", className, ex);
      classFound = null;
    }
    return Optional.ofNullable(classFound);
  }

  default Optional<String> getProperty(final PropertyResolver propertyResolver, final String key) {
    final Optional<String> result;
    if (propertyResolver != null) {
      final String property = propertyResolver.getProperty(key);
      result = Optional.ofNullable(property);
    } else {
      result = Optional.empty();
    }
    return result;
  }

  default <M extends ObjectMapper> Boolean isXmlMapper(final M mapper) {
    final Optional<Class<?>> optional = findClass(XMLMAPPER_CLASSNAME);
    return optional.map(xmlMapperClass -> xmlMapperClass.isAssignableFrom(mapper.getClass())).orElse(FALSE);
  }

  // ---------------------------------------- ----------------------------------------

  /**
   * Build-in jackson2 configurators.
   *
   * @author zhanghaolun
   */
  @Slf4j
  enum BuildinJackson2Configurators implements Jackson2Configurator<BuildinJackson2Configurators> {
    JACKSON2_DEFAULT_CONFIGURATOR {
      @Override
      public <M extends ObjectMapper> M config(final PropertyResolver propertyResolver, final M mapper) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);

        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS, false);
        mapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
        return mapper;
      }
    },
    JACKSON2_DATETIME_CONFIGURATOR {
      @Override
      public <M extends ObjectMapper> M config(final PropertyResolver propertyResolver, final M mapper) {
        mapper.setTimeZone(Defaults.UTC_P8.toTimeZone());
        mapper.registerModule(new JodaModule());
        // Jdk8Module ?
        mapper.disable(WRITE_DATES_AS_TIMESTAMPS);
        // disable WRITE_DATES_WITH_ZONE_ID ?
        // ISODateTimeFormat.basicDateTime()
        final DateFormat formatJdk = new SimpleDateFormat(Defaults.PATTERN_JAVA_ISO8601);
        formatJdk.setTimeZone(UTC_P8.toTimeZone());
        mapper.setDateFormat(formatJdk);
        return mapper;
      }
    },
    JACKSON2_HAL_CONFIGURATOR {

      private static final String MODULE_CLASS = "org.springframework.hateoas.hal.Jackson2HalModule";

      @Override
      public <M extends ObjectMapper> M config(final PropertyResolver propertyResolver, final M mapper) {
        final Optional<Class<?>> moduleClass = this.findClass(MODULE_CLASS);
        if (moduleClass.isPresent()) {
          // need HalHandlerInstantiator or lead to exception on data-rest request
          final Boolean isAlreadyRegisteredIn = this.isAlreadyRegisteredIn(mapper, moduleClass.get());
          if (!isAlreadyRegisteredIn) {
            try {
              mapper.registerModule((Module) moduleClass.get().newInstance());
            } catch (final ReflectiveOperationException ex) {
              log.info("Jackson2HalModule config error", ex);
            }
          }
        }
        return mapper;
      }

      <M extends ObjectMapper> Boolean isAlreadyRegisteredIn(final M mapper, final Class<?> jackson2HalModuleClass) {
        Boolean result;
        try {
          final Method isAlreadyRegisteredIn = jackson2HalModuleClass.getDeclaredMethod( //
            "isAlreadyRegisteredIn", ObjectMapper.class);
          result = (Boolean) isAlreadyRegisteredIn.invoke(null, mapper);
        } catch (final ReflectiveOperationException | SecurityException | IllegalArgumentException ex) {
          log.info("Jackson2HalModule config error", ex);
          result = FALSE;
        }
        return result;
      }
    },
    /**
     * see: http://wiki.fasterxml.com/JacksonJAXBAnnotations
     * see: https://github.com/FasterXML/jackson-module-jaxb-annotations
     */
    JACKSON2_JAXB_ANNOTATION_CONFIGUATOR {

      private static final String MODULE_CLASS = "com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule";
      private static final String JAXB_ANNOTATION_INTROSPECTOR_CLASS = //
        "com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector";

      @Override
      public <M extends ObjectMapper> M config(final PropertyResolver propertyResolver, final M mapper) {
        final Boolean isXmlMapper = this.isXmlMapper(mapper);
        final String jaxbEnabledDefault = isXmlMapper ? "true" : "false";
        final Boolean jaxbEnabled = parseBoolean( //
          this.getProperty(propertyResolver, JACKSON_JAXB_ENABLED).orElse(jaxbEnabledDefault));

        final Optional<Class<?>> moduleClass = this.findClass(MODULE_CLASS);
        if (jaxbEnabled && moduleClass.isPresent()) {
          try {
            final Class<?> jaxbClass = this.findClass(JAXB_ANNOTATION_INTROSPECTOR_CLASS).get();
            final Module module = (Module) moduleClass.get().getConstructor(jaxbClass) //
              .newInstance(new Jackson2HackedJaxbAnnotationIntrospector());
            final Class<?> enumType = this.findClass(MODULE_CLASS + "$Priority").orElse(null);

            final String priorityName = isXmlMapper ? "PRIMARY" : "SECONDARY";
            final Object priority = this.getEnumValue(enumType, priorityName);
            final Method setPriorityMethod = moduleClass.get().getDeclaredMethod("setPriority", enumType);
            setPriorityMethod.invoke(module, priority);

            mapper.registerModule(module);
          } catch (final ReflectiveOperationException ex) {
            log.info("JaxbAnnotationModule config error", ex);
          }
        }
        return mapper;
      }

      private Object getEnumValue(final Class<?> enumType, final String name) throws ReflectiveOperationException {
        final Object result;
        if (enumType != null) {
          final Method method = enumType.getDeclaredMethod("valueOf", String.class);
          result = method.invoke(enumType, name);
        } else {
          result = null;
        }
        return result;
      }
    };

    @Override
    public abstract <M extends ObjectMapper> M config(PropertyResolver propertyResolver, M objectMapper);
  }
}
