package com.yirendai.oss.lib.common;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * Ignore @XmlJavaTypeAdapter.
 * see: https://github.com/FasterXML/jackson-dataformat-xml/issues/37
 */
public class Jackson2HackedJaxbAnnotationIntrospector extends JaxbAnnotationIntrospector {

  public Jackson2HackedJaxbAnnotationIntrospector() {
    super(TypeFactory.defaultInstance());
  }

  @Override
  public Object findSerializationConverter(final Annotated annotated) {
    return null;
  }

  @Override
  public Object findDeserializationConverter(final Annotated annotated) {
    return null;
  }
}
